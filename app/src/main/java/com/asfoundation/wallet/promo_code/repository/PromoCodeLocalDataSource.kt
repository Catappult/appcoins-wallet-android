package com.asfoundation.wallet.promo_code.repository

import com.appcoins.wallet.core.network.backend.model.PromoCodeBonusResponse
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class PromoCodeLocalDataSource @Inject constructor(
  private val promoCodeDao: PromoCodeDao,
  private val rxSchedulers: RxSchedulers
) {

  fun savePromoCode(
    promoCodeBonus: PromoCodeBonusResponse,
    validity: ValidityState
  ): Single<PromoCodeEntity> {
    return Single.just(
      PromoCodeEntity(
        promoCodeBonus.code,
        promoCodeBonus.bonus,
        validity.value,
        promoCodeBonus.app.appName,
        promoCodeBonus.app.packageName,
        promoCodeBonus.app.appIcon
      )
    )
      .doOnSuccess { promoCodeEntity ->
        promoCodeDao.replaceSavedPromoCodeBy(promoCodeEntity)
      }

      .subscribeOn(rxSchedulers.io)
  }

  fun observeSavedPromoCode(): Observable<PromoCode> {
    return promoCodeDao.getSavedPromoCode()
      .map {
        if (it.isEmpty()) PromoCode(null, null, null, null)
        else PromoCode(
          it[0].code,
          it[0].bonus,
          ValidityState.toEnum(it[0].validityState ?: ValidityState.ERROR.value),
          it[0].appName
        )
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