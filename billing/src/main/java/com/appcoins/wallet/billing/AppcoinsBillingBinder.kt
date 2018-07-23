package com.appcoins.wallet.billing

import android.os.Bundle
import com.appcoins.billing.AppcoinsBilling
import com.appcoins.wallet.billing.repository.BillingSupportedType
import com.appcoins.wallet.billing.repository.entity.Purchase
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.ArrayList

internal class AppcoinsBillingBinder(private val billing: Billing,
                                     private val supportedVersion: Int,
                                     private val walletService: WalletService) :
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

    internal const val RESPONSE_CODE = "RESPONSE_CODE"
    internal const val INAPP_PURCHASE_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST"
    internal const val INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST"
    internal const val INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST"


    internal const val ITEM_TYPE_INAPP = "inapp"
    internal const val ITEM_TYPE_SUBS = "subs"
  }

  override fun isBillingSupported(apiVersion: Int, packageName: String?, type: String?): Int {
    if (apiVersion != supportedVersion || packageName == null || packageName.isBlank() || type == null || type.isBlank()) {
      return RESULT_BILLING_UNAVAILABLE
    }
    return when (type) {
      ITEM_TYPE_INAPP -> {
        billing.isInAppSupported(packageName)
            .subscribeOn(Schedulers.io())
            .map { supported -> mapSupported(supported) }.blockingGet()
      }
      ITEM_TYPE_SUBS -> {
        billing.isSubsSupported(packageName)
            .subscribeOn(Schedulers.io())
            .map { isSupported -> mapSupported(isSupported) }.blockingGet()
      }
      else -> RESULT_BILLING_UNAVAILABLE
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
    val result = Bundle()

    if (apiVersion != supportedVersion) {
      result.putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR)
      return result
    }

    val dataList = ArrayList<String>()
    val signatureList = ArrayList<String>()
    val skuList = ArrayList<String>()

    if (type == ITEM_TYPE_INAPP) {
      try {
        val address = walletService.getWalletAddress().blockingGet()
        val signature = walletService.signContent(address).blockingGet()
        val purchases = billing.getPurchases(packageName!!, address, signature, BillingSupportedType.INAPP)
            .blockingGet()

        purchases.forEach { purchase: Purchase ->
          dataList.add(purchase.getSignatureData())
          signatureList.add(purchase.signature.value)
          skuList.add(purchase.product.name)
        }
      } catch (exception: Exception) {
        result.putInt(RESPONSE_CODE, mapPaymentThrowable(exception.cause))
        return result
      }

    }

    result.putStringArrayList(INAPP_PURCHASE_DATA_LIST, dataList)
    result.putStringArrayList(INAPP_PURCHASE_ITEM_LIST, skuList)
    result.putStringArrayList(INAPP_DATA_SIGNATURE_LIST, signatureList)
    result.putInt(RESPONSE_CODE, RESULT_OK)
    return result
  }

  override fun consumePurchase(apiVersion: Int, packageName: String?, purchaseToken: String?): Int {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private fun mapSupported(supportType: Billing.BillingSupportType): Int =
      when (supportType) {
        Billing.BillingSupportType.SUPPORTED -> RESULT_OK
        Billing.BillingSupportType.MERCHANT_NOT_FOUND -> RESULT_BILLING_UNAVAILABLE
        Billing.BillingSupportType.UNKNOWN_ERROR -> RESULT_BILLING_UNAVAILABLE
        Billing.BillingSupportType.NO_INTERNET_CONNECTION -> RESULT_SERVICE_UNAVAILABLE
        Billing.BillingSupportType.API_ERROR -> RESULT_ERROR
      }

  private fun mapPaymentThrowable(throwable: Throwable?): Int =
    when (throwable) {
      is IOException -> RESULT_SERVICE_UNAVAILABLE
      is IllegalArgumentException -> RESULT_DEVELOPER_ERROR
      else -> RESULT_ERROR
    }


}