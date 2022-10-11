package com.asfoundation.wallet.promotions.usecases

import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.asfoundation.wallet.entity.Wallet
import javax.inject.Inject

class GetVipReferralUseCase @Inject constructor(
  private val promotionsRepository: PromotionsRepository
) {

  operator fun invoke(wallet: Wallet) = promotionsRepository.getVipReferral(wallet.address)
}