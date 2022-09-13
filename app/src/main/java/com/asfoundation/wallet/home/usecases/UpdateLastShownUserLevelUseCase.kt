package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import javax.inject.Inject

class UpdateLastShownUserLevelUseCase @Inject constructor(private val promotionsRepository: PromotionsRepository) {

  operator fun invoke(address: String, currentLevel: Int) {
    return promotionsRepository.shownLevel(
      address, currentLevel,
      GamificationContext.NOTIFICATIONS_LEVEL_UP
    )
  }
}