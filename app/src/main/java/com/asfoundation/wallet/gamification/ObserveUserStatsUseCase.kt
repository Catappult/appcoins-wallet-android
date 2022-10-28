package com.asfoundation.wallet.gamification

import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.PromotionsGamificationStats
import com.asfoundation.wallet.home.usecases.FindDefaultWalletUseCase
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import io.reactivex.Observable
import javax.inject.Inject

class ObserveUserStatsUseCase @Inject constructor(
  private val gamification: Gamification,
  private val findDefaultWalletUseCase: FindDefaultWalletUseCase,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase
) {

  operator fun invoke(): Observable<PromotionsGamificationStats> {
    return getCurrentPromoCodeUseCase()
      .flatMapObservable { promoCode ->
        findDefaultWalletUseCase()
          .flatMapObservable { gamification.getUserStats(it.address, promoCode.code) }
      }
  }
}