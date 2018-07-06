package com.appcoins.wallet.billing

import android.os.Bundle
import com.appcoins.billing.AppcoinsBilling

class AppcoinsBillingBinder(var billing: Billing) : AppcoinsBilling.Stub() {
  override fun isBillingSupported(apiVersion: Int, packageName: String?, type: String?): Int {
    return billing.isSupported().map { t -> if (t) 1 else 0 }.blockingGet()
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