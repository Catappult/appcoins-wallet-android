package com.asfoundation.wallet.promotions.usecases

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import javax.inject.Inject

class GetVipReferralUseCase
@Inject
constructor(private val promotionsRepository: PromotionsRepository) {

  operator fun invoke(wallet: Wallet) = promotionsRepository.getVipReferral(wallet.address)
}
