package com.asfoundation.wallet.promo_code.repository

import com.asfoundation.wallet.base.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class PromoCodeLocalDataSource @Inject constructor(private val promoCodeDao: PromoCodeDao,
                                                   private val rxSchedulers: RxSchedulers) {

  fun savePromoCode(promoCodeBonus: PromoCodeBonusResponse, expired: Boolean): Single<PromoCodeEntity> {
    return Single.just(
        PromoCodeEntity(
          promoCodeBonus.code,
          promoCodeBonus.bonus,
          expired,
          promoCodeBonus.app.appName,
          promoCodeBonus.app.packageName,
          promoCodeBonus.app.appIcon
        ))
        .doOnSuccess { promoCodeEntity ->
          promoCodeDao.replaceSavedPromoCodeBy(promoCodeEntity)
        }

        .subscribeOn(rxSchedulers.io)
  }

  fun observeSavedPromoCode(): Observable<PromoCode> {
    return promoCodeDao.getSavedPromoCode()
        .map {
          if (it.isEmpty()) PromoCode(null, null, null, null)
          else PromoCode(it[0].code, it[0].bonus, it[0].expired, it[0].appName)
        }
        .subscribeOn(rxSchedulers.io)
  }

  fun getSavedPromoCode(): Single<PromoCode> {
    return observeSavedPromoCode().firstOrError()
        .subscribeOn(rxSchedulers.io)
  }

  fun removePromoCode(): Completable {
    return Completable.fromAction { promoCodeDao.removeSavedPromoCode() }
        .subscribeOn(rxSchedulers.io)
  }
}