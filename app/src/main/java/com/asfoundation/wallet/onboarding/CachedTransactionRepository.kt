package com.asfoundation.wallet.onboarding

import com.appcoins.wallet.core.network.backend.api.CachedTransactionApi
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.sharedpreferences.OnboardingPaymentSdkPreferencesDataSource
import io.reactivex.Single
import javax.inject.Inject

class CachedTransactionRepository @Inject constructor(
  val api: CachedTransactionApi,
  val rxSchedulers: RxSchedulers,
  private val onboardingPaymentSdkPreferencesDataSource: OnboardingPaymentSdkPreferencesDataSource
) {

  fun getCachedTransaction(): Single<CachedTransaction> {
    return api.getCachedTransaction().subscribeOn(rxSchedulers.io).map { response ->
      setWsPort(response.wsPort)
      CachedTransaction(
        response.referrerUrl,
        response.product,
        response.domain,
        response.callbackUrl,
        response.currency,
        response.orderReference,
        response.value,
        response.signature,
        response.origin,
        response.type,
        response.oemId,
        response.wsPort,
        response.metadata
      )
    }.onErrorReturn {
      CachedTransaction(null, null, null, null, null, null, 0.0, null, null, null, null, null, null)
    }
  }

  private fun setWsPort(wsPort: String?) {
    if (wsPort != null) {
      onboardingPaymentSdkPreferencesDataSource.setWsPort(wsPort)
    }
  }

  fun getWsPort() : String? {
    return onboardingPaymentSdkPreferencesDataSource.getWsPort()
  }

  fun setResponseCodeToPaymentWebSocket(responseCode: Int) {
    onboardingPaymentSdkPreferencesDataSource.setResponseCodeWebSocket(responseCode)
  }

  fun getResponseCodeToPaymentWebSocket() : Int {
    return onboardingPaymentSdkPreferencesDataSource.getResponseCodeWebSocket()
  }

  companion object {
    const val PAYMENT_TYPE_SDK = "INAPP"
    const val PAYMENT_TYPE_OSP = "INAPP_UNMANAGED"
  }

}

data class CachedTransaction(
  var referrerUrl: String?,
  val sku: String?,
  val packageName: String?,
  var callbackUrl: String?,
  var currency: String?,
  val orderReference: String?,
  var value: Double,
  val signature: String?,
  val origin: String?,
  var type: String?,
  var oemId: String?,
  var wsPort: String?,
  var metadata: String?
)

