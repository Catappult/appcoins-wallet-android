package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.PromotionsGamificationStats
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import io.reactivex.Single
import javax.inject.Inject

class GetLastShownUserLevelUseCase @Inject constructor(private val promotionsRepository: PromotionsRepository) {

  operator fun invoke(address: String): Single<Int> {
    return promotionsRepository.getLastShownLevel(
      address,
      GamificationContext.NOTIFICATIONS_LEVEL_UP
    )
      .map { if (it == PromotionsGamificationStats.INVALID_LEVEL) 0 else it }
  }
}