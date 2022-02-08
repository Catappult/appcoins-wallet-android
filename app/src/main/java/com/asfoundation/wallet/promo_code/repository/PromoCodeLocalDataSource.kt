package com.asfoundation.wallet.promo_code.repository

import com.asfoundation.wallet.base.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class PromoCodeLocalDataSource(private val promoCodeDao: PromoCodeDao,
                               private val rxSchedulers: RxSchedulers) {

  fun savePromoCode(promoCode: PromoCodeResponse,
                    promoCodeBonus: PromoCodeBonusResponse): Single<PromoCodeEntity> {
    return Single.just(
        PromoCodeEntity(promoCode.code, promoCodeBonus.bonus, promoCode.expiry, promoCode.expired,
            promoCodeBonus.app.appName, promoCodeBonus.app.packageName, promoCodeBonus.app.appIcon))
        .doOnSuccess { promoCodeEntity ->
          promoCodeDao.replaceSavedPromoCodeBy(promoCodeEntity)
        }
        .subscribeOn(rxSchedulers.io)
  }

  fun observeSavedPromoCode(): Observable<PromoCode> {
    return promoCodeDao.getSavedPromoCode()
        .map {
          if (it.isEmpty()) PromoCode(null, null, null, null, null)
          else PromoCode(it[0].code, it[0].bonus, it[0].expiryDate, it[0].expired, it[0].appName)
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