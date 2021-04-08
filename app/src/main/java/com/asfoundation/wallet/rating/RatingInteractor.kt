package com.asfoundation.wallet.rating

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.util.*

class RatingInteractor(private val ratingRepository: RatingRepository,
                       private val gamificationInteractor: GamificationInteractor,
                       private val walletService: WalletService,
                       private val ioScheduler: Scheduler) {

  companion object {
    const val MINIMUM_TRANSACTIONS_NR = 5
  }

  fun shouldOpenRatingDialog(): Single<Boolean> {
    val remindMeLaterDate = ratingRepository.getRemindMeLaterDate()
    if (remindMeLaterDate > -1L && remindMeLaterDate <= System.currentTimeMillis()) {
      return Single.just(true)
    }
    if (!ratingRepository.hasSeenDialog()) {
      return gamificationInteractor.getUserLevel()
          .map { level -> level >= 6 || ratingRepository.hasEnoughSuccessfulTransactions() }
    }
    return Single.just(false)
  }

  fun updateTransactionsNumber(transactions: List<Transaction>) {
    var transactionsNumber = 0
    for (transaction in transactions) {
      if ((transaction.type == Transaction.TransactionType.IAP
              || transaction.type == Transaction.TransactionType.TOP_UP
              || transaction.type == Transaction.TransactionType.IAP_OFFCHAIN)
          && transaction.status == Transaction.TransactionStatus.SUCCESS) {
        if (++transactionsNumber >= MINIMUM_TRANSACTIONS_NR) {
          ratingRepository.saveEnoughSuccessfulTransactions()
          break
        }
      }
    }
  }

  fun sendUserFeedback(feedbackText: String): Completable {
    return walletService.getWalletAddress()
        .flatMap { address ->
          ratingRepository.sendFeedback(address.toLowerCase(Locale.ROOT), feedbackText)
        }
        .ignoreElement()
        .subscribeOn(ioScheduler)
  }

  fun isNotFirstTime(): Boolean = ratingRepository.isNotFirstTime()

  fun setRemindMeLater() = ratingRepository.setRemindMeLater()

  fun setImpression() = ratingRepository.setImpression()
}