package com.asfoundation.wallet.promo_code.repository

import android.util.Log
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.http.GET
import retrofit2.http.Path

class PromoCodeRepository(private val promoCodeApi: PromoCodeApi,
                          private val promoCodeBackendApi: PromoCodeBackendApi,
                          private val promoCodeDao: PromoCodeDao) {

  fun setPromoCode(promoCodeString: String): Completable {
    return promoCodeApi.getPromoCode(promoCodeString)
        .flatMap {
          promoCodeBackendApi.getPromoCode(promoCodeString).map { bonus ->
            PromoCodeEntity(it.code, bonus.bonus, it.expiry, it.expired)
          }
        }
        .flatMapCompletable {
          Completable.fromAction { promoCodeDao.replaceSavedPromoCodeBy(it) }
        }
        .subscribeOn(Schedulers.io())
  }

  fun getCurrentPromoCode(): Observable<PromoCodeEntity> {
    return promoCodeDao.getSavedPromoCode()
        .map {
          if (it.isEmpty()) PromoCodeEntity("",0.0, "", null) else it[0]
        }.doOnNext { Log.d("APPC-2709", "PromoCodeRepository: getCurrentPromoCode: $it") }
        .subscribeOn(Schedulers.io())
  }

  fun removePromoCode(): Completable {
    return Completable.fromAction { promoCodeDao.removeSavedPromoCode() }
        .subscribeOn(Schedulers.io())
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