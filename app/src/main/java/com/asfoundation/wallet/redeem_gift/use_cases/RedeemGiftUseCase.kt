package com.asfoundation.wallet.redeem_gift.use_cases

import com.asfoundation.wallet.redeem_gift.repository.RedeemGiftRepository
import com.asfoundation.wallet.repository.WalletRepository
import io.reactivex.Completable
import javax.inject.Inject

class RedeemGiftUseCase @Inject constructor(
    private val redeemGiftRepository: RedeemGiftRepository,
    private val walletRepository: WalletRepository
) {

  operator fun invoke(redeemGiftCode: String): Completable {

    //TODO get wallet address from repo

    return redeemGiftRepository.setRedeemGift(redeemGiftCode)
  }
}