package com.asfoundation.wallet.redeem_gift.use_cases

import com.asfoundation.wallet.redeem_gift.repository.RedeemCode
import com.asfoundation.wallet.redeem_gift.repository.RedeemGiftRepository
import io.reactivex.Single
import javax.inject.Inject

class RedeemGiftUseCase @Inject constructor(
  private val redeemGiftRepository: RedeemGiftRepository,
) {

  operator fun invoke(redeemGiftCode: String): Single<RedeemCode> =
    redeemGiftRepository.redeemGift(redeemGiftCode)

}
