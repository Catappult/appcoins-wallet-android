package com.asfoundation.wallet.gamification

import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.PromotionsGamificationStats
import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import com.asfoundation.wallet.home.usecases.FindDefaultWalletUseCase
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import io.reactivex.Observable
import javax.inject.Inject

class ObserveUserStatsUseCase @Inject constructor(
  private val gamification: Gamification,
  private val findDefaultWalletUseCase: FindDefaultWalletUseCase,
  private val getCurrentPromoCodeUseCase: com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
) {

  operator fun invoke(): Observable<PromotionsGamificationStats> {
    return getCurrentPromoCodeUseCase()
      .flatMapObservable { promoCode ->
        findDefaultWalletUseCase()
          .flatMapObservable { gamification.getUserStats(it.address, promoCode.code) }
      }
      .onErrorReturn {
        PromotionsGamificationStats(
          resultState = PromotionsGamificationStats.ResultState.UNKNOWN_ERROR,
          gamificationStatus = GamificationStatus.NONE
        )
      }
  }
}