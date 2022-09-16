package com.asfoundation.wallet.main.use_cases

import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.entity.GamificationStatus
import com.asfoundation.wallet.home.usecases.FindDefaultWalletUseCase
import io.reactivex.Single
import javax.inject.Inject

class IsNewVipUseCase @Inject constructor(
  private val promotionsRepository: PromotionsRepository,
  private val findDefaultWalletUseCase: FindDefaultWalletUseCase
) {

  operator fun invoke(): Single<Boolean> {
    return findDefaultWalletUseCase()
      .flatMap { wallet ->
        promotionsRepository.getGamificationStats(wallet.address, null)
          .map { stats ->
            val isVipCalloutAlreadySeen = promotionsRepository.isVipCalloutAlreadySeen()
            System.out.println("AAAAAAAAAAAAAAA ${stats.gamificationStatus == GamificationStatus.VIP} ${isVipCalloutAlreadySeen}")
            stats.gamificationStatus == GamificationStatus.VIP && !isVipCalloutAlreadySeen
          }
          .firstOrError()
      }
  }

}
