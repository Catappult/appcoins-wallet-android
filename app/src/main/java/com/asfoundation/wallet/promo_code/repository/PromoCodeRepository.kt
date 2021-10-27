package com.asfoundation.wallet.promo_code.repository

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.http.GET
import retrofit2.http.Path

class PromoCodeRepository(private val promoCodeApi: PromoCodeApi,
                          private val promoCodeDao: PromoCodeDao) {

  fun setPromoCode(promoCodeString: String) {
    promoCodeApi.getPromoCode(promoCodeString)
        .map {
          PromoCodeEntity(it.code, it.expiry, it.expired)
        }
        .doOnNext {
          promoCodeDao.replaceSavedPromoCodeBy(it)
        }
        .subscribeOn(Schedulers.io())
        .subscribe() //TODO WHYYYYYY? its being subscribed in the viewModel, ask aleixo
  }

  fun getCurrentPromoCode(): Single<PromoCodeEntity> {
    return promoCodeDao.hasPromoCode()
        .flatMap {
          promoCodeDao.getSavedPromoCode()
        }
        .onErrorReturn {
          PromoCodeEntity("", "", null)
        }
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
}