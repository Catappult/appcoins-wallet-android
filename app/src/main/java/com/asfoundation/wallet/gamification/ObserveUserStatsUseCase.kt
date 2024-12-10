package com.asfoundation.wallet.gamification

import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.PromotionsGamificationStats
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class ObserveUserStatsUseCase @Inject constructor(
  private val gamification: Gamification,
  private val walletRepository: WalletRepositoryType,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase
) {

  operator fun invoke(offlineFirst: Boolean = false): Observable<PromotionsGamificationStats> {
    return Single.zip(
      getCurrentPromoCodeUseCase(),
      walletRepository.getDefaultWallet()
    ) { promoCode, wallet -> wallet to promoCode }
      .flatMapObservable { (wallet, promoCode) ->
        gamification.getUserStats(
          wallet = wallet.address,
          promoCodeString = promoCode.code,
          offlineFirst = offlineFirst
        )
      }
      .onErrorReturn {
        PromotionsGamificationStats(
          resultState = PromotionsGamificationStats.ResultState.UNKNOWN_ERROR,
          gamificationStatus = GamificationStatus.NONE
        )
      }
  }
}
