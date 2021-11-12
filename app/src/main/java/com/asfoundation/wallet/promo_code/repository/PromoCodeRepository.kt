package com.asfoundation.wallet.promo_code.repository

import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.base.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

class PromoCodeRepository(private val promoCodeApi: PromoCodeApi,
                          private val promoCodeBackendApi: PromoCodeBackendApi,
                          private val promoCodeDao: PromoCodeDao,
                          private val analyticsSetup: AnalyticsSetup,
                          private val rxSchedulers: RxSchedulers) {

  fun setPromoCode(promoCodeString: String): Completable {
    return promoCodeApi.getPromoCode(promoCodeString)
        .flatMap {
          promoCodeBackendApi.getPromoCode(promoCodeString)
              .map { bonus ->
                PromoCodeEntity(it.code, bonus.bonus, it.expiry, it.expired)
              }
        }
        .doOnNext {
          analyticsSetup.setPromoCode(PromoCode(it.code, it.bonus, it.expiryDate, it.expired))
        }
        .flatMapCompletable {
          Completable.fromAction { promoCodeDao.replaceSavedPromoCodeBy(it) }
        }
        .subscribeOn(rxSchedulers.io)
  }

  fun getCurrentPromoCode(): Observable<PromoCode> {
    return promoCodeDao.getSavedPromoCode()
        .map {
          if (it.isEmpty()) PromoCode(null, null, null, null)
          else PromoCode(it[0].code, it[0].bonus, it[0].expiryDate, it[0].expired)
        }
        .subscribeOn(rxSchedulers.io)
  }

  fun removePromoCode(): Completable {
    return Completable.fromAction { promoCodeDao.removeSavedPromoCode() }
        .subscribeOn(rxSchedulers.io)
  }

  interface PromoCodeApi {
    @GET("broker/8.20210201/entity/promo-code/{promoCodeString}")
    fun getPromoCode(
        @Path("promoCodeString") promoCodeString: String): Observable<PromoCodeResponse>
  }

  interface PromoCodeBackendApi {
    @GET("gamification/perks/promo_code/{promoCodeString}/")
    fun getPromoCode(
        @Path("promoCodeString") promoCodeString: String): Observable<PromoCodeBonusResponse>
  }
}