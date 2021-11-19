package com.asfoundation.wallet.promo_code.repository

import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.base.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

class PromoCodeRepository(private val promoCodeApi: PromoCodeApi,
                          private val promoCodeBackendApi: PromoCodeBackendApi,
                          private val promoCodeLocalDataSource: PromoCodeLocalDataSource,
                          private val analyticsSetup: AnalyticsSetup,
                          private val rxSchedulers: RxSchedulers) {

  fun setPromoCode(promoCodeString: String): Completable {
    return Single.zip(promoCodeApi.getPromoCode(promoCodeString),
        promoCodeBackendApi.getPromoCodeBonus(promoCodeString), { promoCode, bonus ->
      Pair(promoCode, bonus)
    })
        .flatMap { promoCodePair ->
          promoCodeLocalDataSource.savePromoCode(promoCodePair.first, promoCodePair.second)
        }
        .doOnSuccess {
          analyticsSetup.setPromoCode(
              PromoCode(it.code, it.bonus, it.expiryDate, it.expired, it.appName))
        }
        .ignoreElement()
        .subscribeOn(rxSchedulers.io)
  }

  fun observeCurrentPromoCode(): Observable<PromoCode> =
      promoCodeLocalDataSource.observeSavedPromoCode()

  fun removePromoCode(): Completable = promoCodeLocalDataSource.removePromoCode()

  interface PromoCodeApi {
    @GET("broker/8.20210201/entity/promo-code/{promoCodeString}")
    fun getPromoCode(@Path("promoCodeString") promoCodeString: String): Single<PromoCodeResponse>
  }

  interface PromoCodeBackendApi {
    @GET("gamification/perks/promo_code/{promoCodeString}/")
    fun getPromoCodeBonus(
        @Path("promoCodeString") promoCodeString: String): Single<PromoCodeBonusResponse>
  }
}