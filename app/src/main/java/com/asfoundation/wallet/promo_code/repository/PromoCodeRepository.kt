package com.asfoundation.wallet.promo_code.repository

import com.appcoins.wallet.core.network.backend.api.PromoCodeBackendApi
import com.appcoins.wallet.core.network.backend.model.PromoCodeBonusResponse
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.appcoins.wallet.core.analytics.analytics.AnalyticsSetup
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

class PromoCodeRepository @Inject constructor(
  private val promoCodeBackendApi: PromoCodeBackendApi,
  private val promoCodeLocalDataSource: PromoCodeLocalDataSource,
  private val analyticsSetup: AnalyticsSetup,
  private val rxSchedulers: RxSchedulers
) {

  fun verifyAndSavePromoCode(promoCodeString: String): Single<PromoCode> {
    return promoCodeBackendApi.getPromoCodeBonus(promoCodeString)
      .subscribeOn(rxSchedulers.io)
      .doOnSuccess { response ->
        analyticsSetup.setPromoCode(
            response.code,
            response.bonus,
            validity = ValidityState.ACTIVE.value,
            response.app.appName
        )
        promoCodeLocalDataSource.savePromoCode(response, ValidityState.ACTIVE).subscribe()
      }
      .map {
        PromoCode(
          it.code,
          it.bonus,
          validity = ValidityState.ACTIVE,
          it.app.appName
        )
      }
      .onErrorReturn {
        val errorCode = (it as? HttpException)?.code()
        handleErrorCodes(errorCode, promoCodeString)
      }
  }

  fun handleErrorCodes(errorCode: Int?, promoCodeString: String): PromoCode {
    val validity = when (errorCode) {
      409 -> {
        promoCodeLocalDataSource.savePromoCode(
          PromoCodeBonusResponse(
            promoCodeString,
            null,
            PromoCodeBonusResponse.App(null, null, null)
          ),
          ValidityState.EXPIRED
        ).subscribe()
        ValidityState.EXPIRED
      }
      404 -> ValidityState.ERROR
      else -> ValidityState.ERROR
    }
    return PromoCode(
      promoCodeString,
      null,
      validity = validity,
      null
    )
  }

  fun observeCurrentPromoCode(): Observable<PromoCode> =
    promoCodeLocalDataSource.observeSavedPromoCode()

  fun removePromoCode(): Completable = promoCodeLocalDataSource.removePromoCode()
}
