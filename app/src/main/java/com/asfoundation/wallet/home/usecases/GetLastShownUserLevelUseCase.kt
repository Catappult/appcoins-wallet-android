package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.GamificationStats
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import io.reactivex.Single
import javax.inject.Inject

class GetLastShownUserLevelUseCase @Inject constructor(private val promotionsRepository: PromotionsRepository) {

  operator fun invoke(address: String): Single<Int> {
    return promotionsRepository.getLastShownLevel(
      address,
      GamificationContext.NOTIFICATIONS_LEVEL_UP
    )
      .map { if (it == GamificationStats.INVALID_LEVEL) 0 else it }
  }
}