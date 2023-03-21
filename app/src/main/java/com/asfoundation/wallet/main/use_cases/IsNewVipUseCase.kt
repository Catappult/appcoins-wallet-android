package com.asfoundation.wallet.main.use_cases

import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import com.asfoundation.wallet.home.usecases.ObserveDefaultWalletUseCase
import io.reactivex.Observable
import javax.inject.Inject

class IsNewVipUseCase @Inject constructor(
  private val promotionsRepository: PromotionsRepository,
  private val observeDefaultWalletUseCase: ObserveDefaultWalletUseCase
) {

  operator fun invoke(): Observable<Boolean> {
    return observeDefaultWalletUseCase()
      .flatMap { wallet ->
        promotionsRepository.getGamificationStats(wallet.address, null)
          .takeUntil {
            it.gamificationStatus != GamificationStatus.NONE
          }
          .map { stats ->
            val isVipCalloutAlreadySeen =
              promotionsRepository.isVipCalloutAlreadySeen(wallet.address)
            stats.gamificationStatus == GamificationStatus.VIP && !isVipCalloutAlreadySeen
          }
      }
  }
}
