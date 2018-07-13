package com.appcoins.wallet.billing

import android.os.Bundle
import com.appcoins.billing.AppcoinsBilling
import io.reactivex.schedulers.Schedulers

internal class AppcoinsBillingBinder(private val billing: Billing,
                                     private val supportedVersion: Int) :
    AppcoinsBilling.Stub() {
  companion object {
    internal const val RESULT_OK = 0 // success
    internal const val RESULT_USER_CANCELED = 1 // user pressed back or canceled a dialog
    internal const val RESULT_SERVICE_UNAVAILABLE = 2 // The network connection is down
    internal const val RESULT_BILLING_UNAVAILABLE =
        3 // this billing API version is not supported for the type requested
    internal const val RESULT_ITEM_UNAVAILABLE = 4 // requested SKU is not available for purchase
    internal const val RESULT_DEVELOPER_ERROR = 5 // invalid arguments provided to the API
    internal const val RESULT_ERROR = 6 // Fatal error during the API action
    internal const val RESULT_ITEM_ALREADY_OWNED =
        7 // Failure to purchase since item is already owned
    internal const val RESULT_ITEM_NOT_OWNED = 8 // Failure to consume since item is not owned
  }

  override fun isBillingSupported(apiVersion: Int, packageName: String?, type: String?): Int {
    if (apiVersion != supportedVersion || packageName == null || packageName.isBlank() || type == null || type.isBlank()) {
      return RESULT_BILLING_UNAVAILABLE
    }
    return when (type) {
      "inapp" -> {
        billing.isInAppSupported(packageName)
            .subscribeOn(Schedulers.io())
            .map { supported -> mapSupported(supported) }.blockingGet()
      }
      "subs" -> {
        billing.isSubsSupported(packageName)
            .subscribeOn(Schedulers.io())
            .map { isSupported -> mapSupported(isSupported) }.blockingGet()
      }
      else -> RESULT_BILLING_UNAVAILABLE
    }

  }

  private fun mapSupported(supportType: Billing.BillingSupportType): Int =
      when (supportType) {
        Billing.BillingSupportType.SUPPORTED -> RESULT_OK
        Billing.BillingSupportType.MERCHANT_NOT_FOUND -> RESULT_BILLING_UNAVAILABLE
        Billing.BillingSupportType.UNKNOWN_ERROR -> RESULT_BILLING_UNAVAILABLE
        Billing.BillingSupportType.NO_INTERNET_CONNECTION -> RESULT_SERVICE_UNAVAILABLE
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