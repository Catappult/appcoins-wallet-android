package com.asfoundation.wallet.redeem_gift.repository

import com.appcoins.wallet.ui.arch.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import javax.inject.Inject

class RedeemGiftRepository @Inject constructor(
  private val redeemGiftBackendApi: RedeemGiftBackendApi,
  private val mapper: RedeemGiftMapper,
  private val rxSchedulers: com.appcoins.wallet.ui.arch.RxSchedulers
) {

  fun redeemGift(giftCode: String, ewt: String): Single<RedeemCode> {
    return redeemGiftBackendApi.redeemGiftCode(giftCode, ewt)
      .subscribeOn(rxSchedulers.io)
      .andThen(Single.just(SuccessfulRedeem as RedeemCode))
      .onErrorReturn { mapper.map(it) }
  }

  interface RedeemGiftBackendApi {
    @POST("gamification/giftcard/{giftcard_key}/redeem")
    fun redeemGiftCode(
      @Path("giftcard_key") giftCode: String,
      @Header("authorization") authorization: String
    ): Completable
  }

}
