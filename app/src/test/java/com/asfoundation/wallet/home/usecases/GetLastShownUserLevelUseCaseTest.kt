package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.PromotionsGamificationStats
import com.appcoins.wallet.gamification.repository.entity.FakePromotionsRepository
import org.junit.Test

internal class GetLastShownUserLevelUseCaseTest {
  private val promotionsRepository = FakePromotionsRepository()
  private val getLastShownUserLevelUseCase = GetLastShownUserLevelUseCase(promotionsRepository)

  @Test
  fun `when getLastShownLevel is called, returns the last shown level for the given address`() {
    val address = "my_wallet"
    val useCase = getLastShownUserLevelUseCase(address)
    val expectedLevel = 3

    promotionsRepository.shownLevel(
      address,
      expectedLevel,
      GamificationContext.NOTIFICATIONS_LEVEL_UP
    )

    useCase.test().assertValue(3)
  }

  @Test
  fun `when there is no level saved, returns 0 for the given address`() {
    val address = "my_wallet"
    val useCase = getLastShownUserLevelUseCase(address)

    promotionsRepository.shownLevel(
      address,
      PromotionsGamificationStats.INVALID_LEVEL,
      GamificationContext.NOTIFICATIONS_LEVEL_UP
    )

    useCase.test().assertValue(0)
  }
}