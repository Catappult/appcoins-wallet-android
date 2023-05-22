package com.asfoundation.wallet.redeem_gift.use_cases

import com.appcoins.wallet.feature.walletInfo.data.authentication.EwtAuthenticatorService
import com.asfoundation.wallet.redeem_gift.repository.RedeemCode
import com.asfoundation.wallet.redeem_gift.repository.RedeemGiftRepository
import com.appcoins.wallet.feature.walletInfo.data.usecases.GetCurrentWalletUseCase
import io.reactivex.Single
import javax.inject.Inject

class RedeemGiftUseCase @Inject constructor(
    private val getCurrentWallet: com.appcoins.wallet.feature.walletInfo.data.usecases.GetCurrentWalletUseCase,
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