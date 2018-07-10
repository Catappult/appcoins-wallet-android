package com.appcoins.wallet.billing

import android.os.Bundle
import com.appcoins.billing.AppcoinsBilling
import io.reactivex.schedulers.Schedulers

internal class AppcoinsBillingBinder(var billing: Billing, private var supportedVersion: Int) :
    AppcoinsBilling.Stub() {
  companion object {
    private const val BILLING_NOT_SUPPORTED_RESPONSE = -1
    private const val BILLING_SUPPORTED_RESPONSE = 0
  }

  override fun isBillingSupported(apiVersion: Int, packageName: String?, type: String?): Int {
    if (apiVersion != supportedVersion || packageName == null || packageName.isBlank() || type == null || type.isBlank()) {
      return BILLING_NOT_SUPPORTED_RESPONSE
    }
    return when (type) {
      "inapp" -> {
        billing.isInAppSupported(packageName)
            .subscribeOn(Schedulers.io())
            .map { isSupported -> mapBoolean(isSupported) }.blockingGet()
      }
      "subs" -> {
        billing.isSubsSupported(packageName)
            .subscribeOn(Schedulers.io())
            .map { isSupported -> mapBoolean(isSupported) }.blockingGet()
      }
      else ->
        0
    }
  }

  private fun mapBoolean(isSupported: Boolean) =
      if (isSupported) BILLING_SUPPORTED_RESPONSE else BILLING_NOT_SUPPORTED_RESPONSE

  override fun getSkuDetails(apiVersion: Int, packageName: String?, type: String?,
                             skusBundle: Bundle?): Bundle {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getBuyIntent(apiVersion: Int, packageName: String?, sku: String?, type: String?,
                            developerPayload: String?): Bundle {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getPurchases(apiVersion: Int, packageName: String?, type: String?,
                            continuationToken: String?): Bundle {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun consumePurchase(apiVersion: Int, packageName: String?, purchaseToken: String?): Int {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}