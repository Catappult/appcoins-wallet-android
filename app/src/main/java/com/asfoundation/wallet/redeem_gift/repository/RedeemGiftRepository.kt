package com.asfoundation.wallet.redeem_gift.repository

import com.appcoins.wallet.core.network.backend.api.RedeemGiftApi
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import io.reactivex.Single
import javax.inject.Inject

class RedeemGiftRepository @Inject constructor(
  private val redeemGiftApi: RedeemGiftApi,
  private val mapper: RedeemGiftMapper,
  private val rxSchedulers: RxSchedulers
) {

  fun redeemGift(giftCode: String, ewt: String): Single<RedeemCode> {
    return redeemGiftApi.redeemGiftCode(giftCode, ewt)
      .subscribeOn(rxSchedulers.io)
      .andThen(Single.just(SuccessfulRedeem as RedeemCode))
      .onErrorReturn { mapper.map(it) }
  }
}
