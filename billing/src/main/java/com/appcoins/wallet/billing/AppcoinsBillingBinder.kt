package com.appcoins.wallet.billing

import android.content.pm.PackageManager
import android.os.Binder
import android.os.Bundle
import android.os.Parcel
import android.os.RemoteException
import com.appcoins.billing.AppcoinsBilling
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.BillingFactory
import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.billing.mappers.ExternalBillingSerializer
import com.appcoins.wallet.billing.repository.entity.Product
import io.reactivex.Single
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.util.*


internal class AppcoinsBillingBinder(private val supportedApiVersion: Int,
                                     private val billingMessagesMapper: BillingMessagesMapper,
                                     private var packageManager: PackageManager,
                                     private val billingFactory: BillingFactory,
                                     private val serializer: ExternalBillingSerializer,
                                     private val proxyService: ProxyService,
                                     private val intentBuilder: BillingIntentBuilder) :
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
    internal const val INAPP_PURCHASE_ID_LIST = "INAPP_PURCHASE_ID_LIST"

    internal const val INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA"
    internal const val INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE"
    internal const val INAPP_ORDER_REFERENCE = "order_reference"
    internal const val INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN"
    internal const val INAPP_PURCHASE_ID = "INAPP_PURCHASE_ID"

    internal const val ITEM_TYPE_INAPP = "inapp"
    internal const val ITEM_TYPE_SUBS = "subs"
    internal const val DETAILS_LIST = "DETAILS_LIST"
    internal const val ITEM_ID_LIST = "ITEM_ID_LIST"
    internal const val BUY_INTENT = "BUY_INTENT"

    internal const val PRODUCT_NAME = "product_name"
    internal const val EXTRA_DEVELOPER_PAYLOAD = "developer_payload"

    const val EXTRA_BDS_IAP = "bds_iap"
  }

  private lateinit var billing: AndroidBilling
  private lateinit var merchantName: String

  @Throws(RemoteException::class)
  override fun onTransact(code: Int, data: Parcel, reply: Parcel, flags: Int): Boolean {
    merchantName = packageManager.getPackagesForUid(Binder.getCallingUid())!![0]
    billing = AndroidBilling(merchantName, billingFactory.getBilling())
    return super.onTransact(code, data, reply, flags)
  }

  override fun isBillingSupported(apiVersion: Int, packageName: String?, type: String?): Int {
    if (apiVersion != supportedApiVersion || packageName == null || packageName.isBlank() || type == null || type.isBlank()) {
      return RESULT_BILLING_UNAVAILABLE
    }
    return when (type) {
      ITEM_TYPE_INAPP -> {
        billing.isInAppSupported()
      }
      ITEM_TYPE_SUBS -> {
        billing.isSubsSupported()
      }
      else -> Single.just(Billing.BillingSupportType.UNKNOWN_ERROR)
    }.subscribeOn(Schedulers.io())
        .map { supported -> billingMessagesMapper.mapSupported(supported) }
        .blockingGet()
  }

  override fun getSkuDetails(apiVersion: Int, packageName: String?, type: String?,
                             skusBundle: Bundle?): Bundle {
    val result = Bundle()

    if (skusBundle == null || !skusBundle.containsKey(
            ITEM_ID_LIST) || apiVersion != supportedApiVersion || type == null || type.isBlank()) {
      result.putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR)
      return result
    }

    val skus = skusBundle.getStringArrayList(ITEM_ID_LIST)

    if (skus == null || skus.size <= 0) {
      result.putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR)
      return result
    }

    return try {
      val serializedProducts: List<String> = billing.getProducts(skus).onErrorResumeNext {
        it.printStackTrace()
        Single.error(billingMessagesMapper.mapException(it))
      }
          .flatMap { Single.just(serializer.serializeProducts(it)) }.subscribeOn(Schedulers.io())
          .blockingGet()
      billingMessagesMapper.mapSkuDetails(serializedProducts)
    } catch (exception: Exception) {
      exception.printStackTrace()
      billingMessagesMapper.mapSkuDetailsError(exception)
    }
  }

  override fun getBuyIntent(apiVersion: Int, packageName: String, sku: String?, type: String?,
                            developerPayload: String?): Bundle {


    if (apiVersion != supportedApiVersion || type == null || type.isBlank() || sku == null) {
      val result = Bundle()
      result.putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR)
      return result
    }

    val getTokenContractAddress = proxyService.getAppCoinsAddress(BuildConfig.DEBUG)
        .subscribeOn(Schedulers.io())
    val getIabContractAddress = proxyService.getIabAddress(BuildConfig.DEBUG)
        .subscribeOn(Schedulers.io())
    val getSkuDetails = billing.getProducts(listOf(sku))
        .subscribeOn(Schedulers.io())
    val getDeveloperAddress = billing.getDeveloperAddress(packageName)
        .subscribeOn(Schedulers.io())

    return Single.zip(getTokenContractAddress,
        getIabContractAddress, getSkuDetails, getDeveloperAddress,
        Function4 { tokenContractAddress: String, iabContractAddress: String, skuDetails: List<Product>, developerAddress: String ->
          try {
            intentBuilder.buildBuyIntentBundle(tokenContractAddress, iabContractAddress,
                developerPayload, true, packageName, developerAddress, skuDetails[0].sku,
                BigDecimal(skuDetails[0].price.appcoinsAmount), skuDetails[0].title)
          } catch (exception: Exception) {
            billingMessagesMapper.mapBuyIntentError(exception)
          }
        }).onErrorReturn { throwable ->
      billingMessagesMapper.mapBuyIntentError(
          throwable as Exception)
    }.blockingGet()
  }

  override fun getPurchases(apiVersion: Int, packageName: String?, type: String?,
                            continuationToken: String?): Bundle {
    val result = Bundle()

    if (apiVersion != supportedApiVersion) {
      result.putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR)
      return result
    }

    val idsList = ArrayList<String>()
    val dataList = ArrayList<String>()
    val signatureList = ArrayList<String>()
    val skuList = ArrayList<String>()

    if (type == ITEM_TYPE_INAPP) {
      try {
        val purchases =
            billing.getPurchases(BillingSupportedType.INAPP)
                .blockingGet()

        purchases.forEach { purchase: Purchase ->
          idsList.add(purchase.uid)
          dataList.add(serializer.serializeSignatureData(purchase))
          signatureList.add(purchase.signature.value)
          skuList.add(purchase.product.name)
        }
      } catch (exception: Exception) {
        return billingMessagesMapper.mapPurchasesError(exception)
      }

    }

    result.putStringArrayList(INAPP_PURCHASE_ID_LIST, idsList)
    result.putStringArrayList(INAPP_PURCHASE_DATA_LIST, dataList)
    result.putStringArrayList(INAPP_PURCHASE_ITEM_LIST, skuList)
    result.putStringArrayList(INAPP_DATA_SIGNATURE_LIST, signatureList)
    result.putInt(RESPONSE_CODE, RESULT_OK)
    return result
  }

  override fun consumePurchase(apiVersion: Int, packageName: String?, purchaseToken: String): Int {
    if (apiVersion != supportedApiVersion) {
      return RESULT_DEVELOPER_ERROR
    }

    return try {
      billing.consumePurchases(purchaseToken).map { RESULT_OK }.blockingGet()
    } catch (exception: Exception) {
      billingMessagesMapper.mapConsumePurchasesError(exception)
    }
  }
}