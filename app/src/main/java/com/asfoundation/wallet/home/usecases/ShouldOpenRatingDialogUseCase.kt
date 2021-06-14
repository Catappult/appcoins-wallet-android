package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.rating.RatingRepository
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import io.reactivex.Single

class ShouldOpenRatingDialogUseCase(private val ratingRepository: RatingRepository,
                                    private val gamificationInteractor: GamificationInteractor) {

  operator fun invoke(): Single<Boolean> {
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
}