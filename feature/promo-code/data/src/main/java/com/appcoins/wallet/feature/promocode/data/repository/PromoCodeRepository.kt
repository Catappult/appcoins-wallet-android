package com.appcoins.wallet.feature.promocode.data.repository

import com.appcoins.wallet.core.analytics.analytics.AnalyticsSetup
import com.appcoins.wallet.core.network.backend.api.PromoCodeApi
import com.appcoins.wallet.core.network.backend.model.PromoCodeBonusResponse
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.promocode.data.error.UserOwnPromoCode
import com.appcoins.wallet.feature.promocode.data.wallet.WalletAddress
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

class PromoCodeRepository @Inject constructor(
  private val promoCodeApi: PromoCodeApi,
  private val walletAddress: WalletAddress,
  private val promoCodeLocalDataSource: PromoCodeLocalDataSource,
  private val analyticsSetup: AnalyticsSetup,
  private val rxSchedulers: RxSchedulers
) {

  fun verifyAndSavePromoCode(
    promoCodeString: String,
  ): Single<PromoCode> {
    return Single.zip(
      walletAddress.getWalletAddresses().map { it.map { address -> address.lowercase() } },
      promoCodeApi.getPromoCodeBonus(promoCodeString)
    ) { walletAddresses, promoCodeResponse ->
      if (!walletAddresses.contains(promoCodeResponse.ownerWallet?.lowercase())) {
        promoCodeResponse
      } else throw UserOwnPromoCode()
    }
      .subscribeOn(rxSchedulers.io)
      .doOnSuccess { response ->
        analyticsSetup.setPromoCode(
          code = response.code,
          bonus = response.bonus,
          validity = ValidityState.ACTIVE.value,
          appName = response.app.appName
        )
        promoCodeLocalDataSource.savePromoCode(
          promoCodeBonus = response,
          validity = ValidityState.ACTIVE
        ).subscribe()
      }
      .map {
        PromoCode(
          code = it.code,
          bonus = it.bonus,
          validity = ValidityState.ACTIVE,
          appName = it.app.appName
        )
      }
      .onErrorReturn {
        if (it is UserOwnPromoCode) {
          PromoCode(
            code = promoCodeString,
            bonus = null,
            validity = ValidityState.USER_OWN_PROMO_CODE,
            appName = null
          )
        } else {
          val errorCode = (it as? HttpException)?.code()
          handleErrorCodes(errorCode, promoCodeString)
        }
      }
  }

  fun handleErrorCodes(errorCode: Int?, promoCodeString: String): PromoCode {
    val validity = when (errorCode) {
      409 -> {
        promoCodeLocalDataSource.savePromoCode(
          promoCodeBonus = PromoCodeBonusResponse(
            code = promoCodeString,
            bonus = null,
            app = PromoCodeBonusResponse.App(null, null, null),
            ownerWallet = null
          ),
          validity = ValidityState.EXPIRED
        ).subscribe()
        ValidityState.EXPIRED
      }

      404 -> ValidityState.ERROR
      else -> ValidityState.ERROR
    }
    return PromoCode(
      code = promoCodeString,
      bonus = null,
      validity = validity,
      appName = null
    )
  }

  fun observeCurrentPromoCode(): Observable<PromoCode> =
    promoCodeLocalDataSource.observeSavedPromoCode()

  fun removePromoCode(): Completable = promoCodeLocalDataSource.removePromoCode()
}
