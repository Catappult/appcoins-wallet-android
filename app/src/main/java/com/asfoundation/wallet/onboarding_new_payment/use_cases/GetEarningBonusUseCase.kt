package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import io.reactivex.Single
import java.math.BigDecimal
import javax.inject.Inject

class GetEarningBonusUseCase @Inject constructor(
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val gamificationInteractor: GamificationInteractor
) {

  operator fun invoke(packageName: String, amount: BigDecimal): Single<ForecastBonusAndLevel> {
    return getCurrentPromoCodeUseCase()
      .flatMap {
        gamificationInteractor.getEarningBonus(packageName, amount, it.code)
      }
  }
}

