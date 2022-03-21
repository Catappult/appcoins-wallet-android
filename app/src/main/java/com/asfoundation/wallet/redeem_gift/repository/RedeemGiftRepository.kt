package com.asfoundation.wallet.redeem_gift.repository

import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.base.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Inject

class RedeemGiftRepository @Inject constructor(
  private val redeemGiftBackendApi: RedeemGiftBackendApi,
  private val rxSchedulers: RxSchedulers
) {

  fun setPromoCode(giftCode: String): Completable {
    return Single.just(redeemGiftBackendApi.redeemGiftCode(giftCode))
      .map{ response ->
        GiftCode(response.amount, response.error)
      }
      .ignoreElement()
      .subscribeOn(rxSchedulers.io)
  }

  interface RedeemGiftBackendApi {  // TODO
    @GET("gamification/perks/promo_code/{promoCodeString}/")  // TODO integration
    fun redeemGiftCode(
      @Path("redeemGiftString") giftCode: String    //TODO
    //TODO add walletAddress
    ): Single<RedeemGiftResponse>
  }

}