package com.asfoundation.wallet.promo_code.repository

import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.promo_code.PromoCodeResult
import com.asfoundation.wallet.redeem_gift.repository.RedeemCode
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Inject

class PromoCodeRepository @Inject constructor(
  private val promoCodeBackendApi: PromoCodeBackendApi,
  private val promoCodeLocalDataSource: PromoCodeLocalDataSource,
  private val analyticsSetup: AnalyticsSetup,
  private val rxSchedulers: RxSchedulers
) {

  fun setPromoCode(promoCodeString: String): Single<PromoCode> {
    return promoCodeBackendApi.getPromoCodeBonus(promoCodeString)
      .doOnSuccess { response ->
        analyticsSetup.setPromoCode(
          PromoCode(
            response.code,
            response.bonus,
            expired = false,
            response.app.appName
          )
        )
        promoCodeLocalDataSource.savePromoCode(response, expired = false)
      }
      .onErrorReturn {
        it.message
        promoCodeLocalDataSource.savePromoCode(
          PromoCodeBonusResponse(
            promoCodeString,
            null,
            PromoCodeBonusResponse.App(null, null, null)
          ),
          expired = true  //TODO new error
        ).subscribe()
        PromoCodeBonusResponse(
          promoCodeString,
          null,
          expired = true,  //TODO new error
          PromoCodeBonusResponse.App(null, null, null)
        )
      }
      .map {
        PromoCode(
          it.code,
          it.bonus,
          expired = false,
          it.app.appName
        )
      }
      .subscribeOn(rxSchedulers.io)
  }

  fun observeCurrentPromoCode(): Observable<PromoCode> =
    promoCodeLocalDataSource.observeSavedPromoCode()

  fun removePromoCode(): Completable = promoCodeLocalDataSource.removePromoCode()

  interface PromoCodeBackendApi {
    @GET("gamification/perks/promo_code/{promoCodeString}/")
    fun getPromoCodeBonus(
      @Path("promoCodeString") promoCodeString: String
    ): Single<PromoCodeBonusResponse>
  }
}
