package com.appcoins.wallet.billing

import android.os.Bundle
import com.appcoins.billing.AppcoinsBilling
import io.reactivex.schedulers.Schedulers

internal class AppcoinsBillingBinder(var billing: Billing) : AppcoinsBilling.Stub() {
  override fun isBillingSupported(apiVersion: Int, packageName: String?, type: String?): Int {
    if (packageName == null || packageName.isBlank() || type == null || type.isBlank()) {
      return 0
    }
    return when (type) {
      "inapp" -> {
        billing.isInAppSupported(packageName)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t) 1 else 0 }.blockingGet()
      }
      "subs" -> {
        billing.isSubsSupported(packageName)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t) 1 else 0 }.blockingGet()
      }
      else ->
        0
    }
  }

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