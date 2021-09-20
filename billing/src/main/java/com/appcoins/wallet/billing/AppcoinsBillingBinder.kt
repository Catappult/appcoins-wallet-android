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
import com.appcoins.wallet.bdsbilling.exceptions.BillingException
import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.entity.Product
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.Function4
import java.math.BigDecimal
import java.util.*


class AppcoinsBillingBinder(private val supportedApiVersion: Int,
                            private val billingMessagesMapper: BillingMessagesMapper,
                            private var packageManager: PackageManager,
                            private val billingFactory: BillingFactory,
                            private val serializer: ExternalBillingSerializer,
                            private val proxyService: ProxyService,
                            private val intentBuilder: BillingIntentBuilder,
                            private val networkScheduler: Scheduler)
  : AppcoinsBilling.Stub() {
  companion object {
    const val RESULT_OK = 0 // success
    internal const val RESULT_USER_CANCELED = 1 // user pressed back or canceled a dialog
    internal const val RESULT_SERVICE_UNAVAILABLE = 2 // The network connection is down
    internal const val RESULT_BILLING_UNAVAILABLE =
        3 // this billing API version is not supported for the type requested
    internal const val RESULT_ITEM_UNAVAILABLE = 4 // requested SKU is not available for purchase
    internal const val RESULT_DEVELOPER_ERROR = 5 // invalid arguments provided to the API
    internal const val RESULT_ERROR = 6 // Fatal error during the API action
    internal const val RESULT_ITEM_ALREADY_OWNED =
        7 // Failure to purchase since item is already owned

    const val RESPONSE_CODE = "RESPONSE_CODE"
    internal const val INAPP_PURCHASE_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST"
    internal const val INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST"
    internal const val INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST"
    internal const val INAPP_PURCHASE_ID_LIST = "INAPP_PURCHASE_ID_LIST"

    internal const val INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA"
    internal const val INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE"
    internal const val INAPP_ORDER_REFERENCE = "order_reference"
    internal const val INAPP_PURCHASE_ID = "INAPP_PURCHASE_ID"

    internal const val ITEM_TYPE_INAPP = "inapp"
    internal const val ITEM_TYPE_SUBS = "subs"
    internal const val DETAILS_LIST = "DETAILS_LIST"
    internal const val ITEM_ID_LIST = "ITEM_ID_LIST"
    internal const val BUY_INTENT = "BUY_INTENT"
    internal const val BUY_INTENT_RAW = "BUY_INTENT_RAW"

    internal const val PRODUCT_NAME = "product_name"
    internal const val EXTRA_DEVELOPER_PAYLOAD = "developer_payload"

    const val EXTRA_BDS_IAP = "bds_iap"
  }

  private lateinit var billing: AndroidBilling
  private lateinit var merchantName: String

  @Throws(RemoteException::class)
  override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
    merchantName = packageManager.getPackagesForUid(Binder.getCallingUid())!![0]
    billing = AndroidBilling(billingFactory.getBilling())
    return super.onTransact(code, data, reply, flags)
  }

  override fun isBillingSupported(apiVersion: Int, packageName: String?,
                                  billingType: String?): Int {
    if (apiVersion != supportedApiVersion || packageName.isNullOrBlank() || billingType.isNullOrBlank()) {
      return RESULT_BILLING_UNAVAILABLE
    }
    return when (billingType) {
      ITEM_TYPE_INAPP -> billing.isInAppSupported(merchantName)
      ITEM_TYPE_SUBS -> billing.isSubsSupported(merchantName)
      else -> Single.just(Billing.BillingSupportType.UNKNOWN_ERROR)
    }.subscribeOn(networkScheduler)
        .map { supported -> billingMessagesMapper.mapSupported(supported) }
        .blockingGet()
  }

  override fun getSkuDetails(apiVersion: Int, packageName: String?, billingType: String?,
                             skusBundle: Bundle?): Bundle {
    val result = Bundle()

    if (skusBundle == null
        || !skusBundle.containsKey(ITEM_ID_LIST)
        || apiVersion != supportedApiVersion
        || billingType == null
        || billingType.isBlank()) {
      with(result) { putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR) }
      return result
    }

    val skus = skusBundle.getStringArrayList(ITEM_ID_LIST)

    if (skus.isNullOrEmpty()) {
      with(result) { putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR) }
      return result
    }

    val type = try {
      BillingSupportedType.valueOfItemType(billingType)
    } catch (e: Exception) {
      with(result) { putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR) }
      return result
    }

    return try {
      val serializedProducts = billing.getProducts(merchantName, skus, type)
          .doOnError { it.printStackTrace() }
          .onErrorResumeNext {
            Single.error(billingMessagesMapper.mapException(it))
          }
          .flatMap { Single.just(serializer.serializeProducts(it)) }
          .subscribeOn(networkScheduler)
          .blockingGet()
      billingMessagesMapper.mapSkuDetails(serializedProducts)
    } catch (exception: Exception) {
      exception.printStackTrace()
      billingMessagesMapper.mapSkuDetailsError(exception)
    }
  }

  override fun getBuyIntent(apiVersion: Int, packageName: String, sku: String?,
                            billingType: String?, developerPayload: String?): Bundle {
    if (validateGetBuyIntentArgs(apiVersion, billingType, sku)) {
      return Bundle().apply {
        putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR)
      }
    }
    requireNotNull(billingType!!)
    requireNotNull(sku!!)

    val type = try {
      BillingSupportedType.valueOfItemType(billingType)
    } catch (e: Exception) {
      return Bundle().apply {
        putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR)
      }
    }

    val getTokenContractAddress = proxyService.getAppCoinsAddress(BuildConfig.DEBUG)
        .subscribeOn(networkScheduler)
    val getIabContractAddress = proxyService.getIabAddress(BuildConfig.DEBUG)
        .subscribeOn(networkScheduler)
    val getSkuDetails = billing.getProducts(merchantName, listOf(sku), type)
        .subscribeOn(networkScheduler)
    val getDeveloperAddress = billing.getDeveloperAddress(packageName)
        .subscribeOn(networkScheduler)
    return Single.zip(getTokenContractAddress, getIabContractAddress, getSkuDetails,
        getDeveloperAddress,
        Function4 { tokenContractAddress: String, iabContractAddress: String, skuDetails: List<Product>, developerAddress: String ->
          try {
            val product = skuDetails[0]
            if (type == BillingSupportedType.INAPP_SUBSCRIPTION && product.subscriptionPeriod == null) {
              billingMessagesMapper.mapInvalidSubscriptionData()
            }
            intentBuilder.buildBuyIntentBundle(type.name, tokenContractAddress, iabContractAddress,
                developerPayload, true, packageName, developerAddress, skuDetails[0].sku,
                BigDecimal(product.transactionPrice.appcoinsAmount), product.title, product.subscriptionPeriod,
                product.trialPeriod)
          } catch (exception: Exception) {
            if (skuDetails.isEmpty()) {
              billingMessagesMapper.mapBuyIntentError(
                  Exception(BillingException(RESULT_ITEM_UNAVAILABLE)))
            } else {
              billingMessagesMapper.mapBuyIntentError(exception)
            }
          }
        })
        .onErrorReturn { throwable ->
          billingMessagesMapper.mapBuyIntentError(
              throwable as Exception)
        }
        .blockingGet()
  }

  private fun validateGetBuyIntentArgs(apiVersion: Int, billingType: String?,
                                       sku: String?): Boolean {
    return (apiVersion != supportedApiVersion
        || billingType == null
        || billingType.isBlank()
        || sku == null)
  }

  override fun getPurchases(apiVersion: Int, packageName: String?, billingType: String?,
                            continuationToken: String?): Bundle {
    val result = Bundle()

    if (apiVersion != supportedApiVersion) {
      with(result) { putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR) }
      return result
    }

    val idsList = ArrayList<String>()
    val dataList = ArrayList<String>()
    val signatureList = ArrayList<String>()
    val skuList = ArrayList<String>()

    try {
      val type =
          billingType?.let { BillingSupportedType.valueOfItemType(it) }
              ?: BillingSupportedType.INAPP
      val purchases = billing.getPurchases(merchantName, type)
          .blockingGet()

      purchases.forEach { purchase: Purchase ->
        idsList.add(purchase.uid)
        dataList.add(purchase.signature.message)
        signatureList.add(purchase.signature.value)
        skuList.add(purchase.product.name)
      }
    } catch (exception: Exception) {
      return billingMessagesMapper.mapPurchasesError(exception)
    }

    return result.apply {
      putStringArrayList(INAPP_PURCHASE_ID_LIST, idsList)
      putStringArrayList(INAPP_PURCHASE_DATA_LIST, dataList)
      putStringArrayList(INAPP_PURCHASE_ITEM_LIST, skuList)
      putStringArrayList(INAPP_DATA_SIGNATURE_LIST, signatureList)
      putInt(RESPONSE_CODE, RESULT_OK)
    }

  }

  override fun consumePurchase(apiVersion: Int, packageName: String?, purchaseToken: String): Int {
    if (apiVersion != supportedApiVersion) {
      return RESULT_DEVELOPER_ERROR
    }

    return try {
      billing.consumePurchases(purchaseToken, merchantName)
          .map { RESULT_OK }
          .blockingGet()
    } catch (exception: Exception) {
      billingMessagesMapper.mapConsumePurchasesError(exception)
    }
  }
}