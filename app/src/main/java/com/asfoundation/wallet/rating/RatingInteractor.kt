package com.asfoundation.wallet.rating

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.util.*

class RatingInteractor(private val ratingRepository: RatingRepository,
                       private val walletService: WalletService,
                       private val ioScheduler: Scheduler) {

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