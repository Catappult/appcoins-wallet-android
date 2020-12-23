package com.asfoundation.wallet.rating

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single

class RatingInteractor(private val ratingRepository: RatingRepository,
                       private val gamificationInteractor: GamificationInteractor,
                       private val walletService: WalletService,
                       private val ioScheduler: Scheduler) {

  fun shouldOpenRatingDialog(): Single<Boolean> {
    val remindMeLaterDate = ratingRepository.getRemindMeLaterDate()
    if (remindMeLaterDate > -1L && remindMeLaterDate <= System.currentTimeMillis()) {
      return Single.just(true)
    }
    if (!ratingRepository.hasSeenDialog()) {
      return gamificationInteractor.getUserStats()
          .map { stats -> stats.level >= 6 || ratingRepository.hasEnoughSuccessfulTransactions() }
    }
    return Single.just(false)
  }

  fun updateTransactionsNumber(transactions: List<Transaction>) {
    var transactionsNumber = 0
    for (transaction in transactions) {
      if ((transaction.type == Transaction.TransactionType.IAP
              || transaction.type == Transaction.TransactionType.TOP_UP)
          && transaction.status == Transaction.TransactionStatus.SUCCESS) {
        if (++transactionsNumber >= 5) {
          ratingRepository.saveEnoughSuccessfulTransactions()
          break
        }
      }
    }
  }

  fun sendUserFeedback(feedbackText: String): Completable {
    return walletService.getWalletAddress()
        .flatMap { address -> ratingRepository.sendFeedback(address, feedbackText) }
        .ignoreElement()
        .subscribeOn(ioScheduler)
  }

  fun isNotFirstTime(): Boolean {
    return ratingRepository.isNotFirstTime()
  }

  fun setRemindMeLater() {
    ratingRepository.setRemindMeLater()
  }

  fun setImpression() {
    ratingRepository.setImpression()
  }
}