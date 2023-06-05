package com.asfoundation.wallet.redeem_gift.use_cases

import com.appcoins.wallet.feature.walletInfo.data.authentication.EwtAuthenticatorService
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.asfoundation.wallet.redeem_gift.repository.RedeemCode
import com.asfoundation.wallet.redeem_gift.repository.RedeemGiftRepository
import io.reactivex.Single
import javax.inject.Inject

class RedeemGiftUseCase @Inject constructor(
        private val getCurrentWallet: GetCurrentWalletUseCase,
        private val redeemGiftRepository: RedeemGiftRepository,
        private val ewtObtainer: EwtAuthenticatorService,
) {

  operator fun invoke(redeemGiftCode: String): Single<RedeemCode> {
    return getCurrentWallet()
      .flatMap { wallet ->
        ewtObtainer.getEwtAuthenticationWithAddress(wallet.address)
      }
      .flatMap { ewt ->
        redeemGiftRepository.redeemGift(redeemGiftCode, ewt)
      }
  }
}