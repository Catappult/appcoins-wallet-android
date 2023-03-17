package com.appcoins.wallet.billing

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.WindowManager
import com.appcoins.communication.MessageProcessorActivity
import com.appcoins.wallet.bdsbilling.BdsBilling
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.BillingThrowableCodeMapper
import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.bdsbilling.exceptions.BillingException
import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.*
import com.appcoins.wallet.bdsbilling.repository.entity.Product
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

class AppcoinsBillingReceiverActivity : MessageProcessorActivity() {
  companion object {
    private val TAG = AppcoinsBillingReceiverActivity::class.java.simpleName
    private const val VERSION_VERSION_ARGUMENT = "API_VERSION"
    private const val PACKAGE_NAME_ARGUMENT = "PACKAGE_NAME"
    private const val BILLING_TYPE = "BILLING_TYPE"
    private const val BILLING_SKU = "BILLING_SKU"
    private const val DEVELOPER_PAYLOAD = "DEVELOPER_PAYLOAD"
    private const val PURCHASE_TOKEN = "PURCHASE_TOKEN"
    private const val RESULT_VALUE = "RESULT_VALUE"
    private const val SUPPORTED_API_VERSION = 3
  }

  private lateinit var billing: AndroidBilling
  private lateinit var networkScheduler: Scheduler
  private lateinit var billingMessagesMapper: BillingMessagesMapper
  private lateinit var serializer: ExternalBillingSerializer
  private lateinit var proxyService: ProxyService
  private lateinit var intentBuilder: BillingIntentBuilder

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (applicationContext !is BillingDependenciesProvider) {
      throw IllegalArgumentException("application must implement ${BillingDependenciesProvider::class.java.simpleName}")
    }
    window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    serializer = ExternalBillingSerializer()
    val dependenciesProvider = applicationContext as BillingDependenciesProvider
    val bdsBilling = BdsBilling(
      BdsRepository(
        RemoteRepository(
          dependenciesProvider.brokerBdsApi(),
          dependenciesProvider.inappApi(),
          BdsApiResponseMapper(SubscriptionsMapper(), InAppMapper()),
          dependenciesProvider.bdsApiSecondary(),
          dependenciesProvider.subscriptionsApi()
        )
      ),
      dependenciesProvider.walletService(),
      BillingThrowableCodeMapper()
    )


    proxyService = dependenciesProvider.proxyService()
    billingMessagesMapper = dependenciesProvider.billingMessagesMapper()
    networkScheduler = Schedulers.io()
    intentBuilder = BillingIntentBuilder(applicationContext)

    billing = AndroidBilling(bdsBilling)
  }

  override fun processValue(methodId: Int, arguments: Parcelable): Parcelable {
    Log.d(TAG, "processValue() called with: methodId = [$methodId], arguments = [$arguments]")
    val args = arguments as Bundle
    val apiVersion = args.getInt(VERSION_VERSION_ARGUMENT)
    val packageName = args.getString(PACKAGE_NAME_ARGUMENT)

    if (packageName.isNullOrBlank()) {
      val response = Bundle()
      response.putInt(
        AppcoinsBillingBinder.RESPONSE_CODE,
        AppcoinsBillingBinder.RESULT_BILLING_UNAVAILABLE
      )
      return response
    }
    val billingType = args.getString(BILLING_TYPE)
    val sku = args.getString(BILLING_SKU)
    val developerPayload = args.getString(DEVELOPER_PAYLOAD)
    val purchaseToken = args.getString(PURCHASE_TOKEN)

    return when (methodId) {
      0 -> isBillingSupported(apiVersion, packageName, billingType)
      1 -> getSkuDetails(apiVersion, packageName, billingType, args.getBundle("SKUS_BUNDLE"))
      2 -> getPurchases(apiVersion, packageName, billingType)
      3 -> getBuyIntent(apiVersion, packageName, sku, billingType, developerPayload)
      4 -> consumePurchase(apiVersion, packageName, purchaseToken, billingType)
      else -> {
        Log.w(TAG, "Unknown method id for: $methodId")
        return createReturnBundle(Bundle().apply {
          putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_DEVELOPER_ERROR)
        })
      }
    }
  }

  private fun consumePurchase(
    apiVersion: Int,
    packageName: String,
    purchaseToken: String?,
    type: String?
  ): Parcelable {
    if (apiVersion != SUPPORTED_API_VERSION) {
      return Bundle().apply {
        putInt(RESULT_VALUE, AppcoinsBillingBinder.RESULT_BILLING_UNAVAILABLE)
      }
    }

    val result = Bundle()
    if (purchaseToken.isNullOrBlank()) {
      result.putInt(RESULT_VALUE, AppcoinsBillingBinder.RESULT_DEVELOPER_ERROR)
      return result
    }
    val billingType = type?.let { BillingSupportedType.valueOfItemType(it) }
    result.putInt(RESULT_VALUE, try {
      billing.consumePurchases(purchaseToken, packageName, billingType)
        .map { AppcoinsBillingBinder.RESULT_OK }
        .blockingGet()
    } catch (exception: Exception) {
      billingMessagesMapper.mapConsumePurchasesError(exception)
    })
    return result
  }

  private fun getBuyIntent(
    apiVersion: Int,
    packageName: String,
    sku: String?,
    billingType: String?,
    developerPayload: String?
  ): Parcelable {

    if (validateGetBuyIntentArgs(apiVersion, billingType, sku)) {
      return Bundle().apply {
        putInt(
          AppcoinsBillingBinder.RESPONSE_CODE,
          AppcoinsBillingBinder.RESULT_BILLING_UNAVAILABLE
        )
      }
    }
    requireNotNull(billingType!!)
    requireNotNull(sku!!)

    val type = try {
      BillingSupportedType.valueOfItemType(billingType)
    } catch (e: Exception) {
      return Bundle().apply {
        putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_DEVELOPER_ERROR)
      }
    }

    val getTokenContractAddress = proxyService.getAppCoinsAddress(BuildConfig.DEBUG)
      .subscribeOn(networkScheduler)
    val getIabContractAddress = proxyService.getIabAddress(BuildConfig.DEBUG)
      .subscribeOn(networkScheduler)
    val getSkuDetails = billing.getProducts(packageName, listOf(sku), type)
      .subscribeOn(networkScheduler)
    val getDeveloperAddress = billing.getDeveloperAddress(packageName)
      .subscribeOn(networkScheduler)

    return createReturnBundle(Single.zip(
      getTokenContractAddress,
      getIabContractAddress,
      getSkuDetails,
      getDeveloperAddress
    ) { tokenContractAddress: String, iabContractAddress: String, skuDetails: List<Product>, developerAddress: String ->
      try {
        val product = skuDetails[0]
        intentBuilder.buildBuyIntentBundle(
          type.name,
          tokenContractAddress,
          iabContractAddress,
          developerPayload,
          true,
          packageName,
          developerAddress,
          product.sku,
          BigDecimal(product.transactionPrice.appcoinsAmount),
          product.title,
          product.subscriptionPeriod,
          product.trialPeriod
        )
      } catch (exception: Exception) {
        if (skuDetails.isEmpty()) {
          billingMessagesMapper.mapBuyIntentError(
            Exception(
              BillingException(
                AppcoinsBillingBinder.RESULT_ITEM_UNAVAILABLE
              )
            )
          )
        } else {
          billingMessagesMapper.mapBuyIntentError(exception)
        }
      }
    }
      .onErrorReturn { throwable ->
        billingMessagesMapper.mapBuyIntentError(throwable as Exception)
      }
      .blockingGet())
  }

  private fun validateGetBuyIntentArgs(
    apiVersion: Int,
    billingType: String?,
    sku: String?
  ): Boolean {
    return (apiVersion != SUPPORTED_API_VERSION
        || billingType == null
        || billingType.isBlank()
        || sku == null)
  }

  private fun getPurchases(apiVersion: Int, packageName: String, billingType: String?): Parcelable {
    if (apiVersion != SUPPORTED_API_VERSION) {
      return Bundle().apply {
        putInt(
          AppcoinsBillingBinder.RESPONSE_CODE,
          AppcoinsBillingBinder.RESULT_BILLING_UNAVAILABLE
        )
      }
    }

    val result = Bundle()
    val idsList = ArrayList<String>()
    val dataList = ArrayList<String>()
    val signatureList = ArrayList<String>()
    val skuList = ArrayList<String>()

    val type =
      billingType?.let {
        try {
          BillingSupportedType.valueOfItemType(billingType)
        } catch (e: Exception) {
          return Bundle().apply {
            putInt(
              AppcoinsBillingBinder.RESPONSE_CODE,
              AppcoinsBillingBinder.RESULT_DEVELOPER_ERROR
            )
          }
        }
      } ?: BillingSupportedType.INAPP

    try {
      val purchases = billing.getPurchases(packageName, type).blockingGet()

      purchases.forEach { purchase: Purchase ->
        idsList.add(purchase.uid)
        dataList.add(purchase.signature.message)
        signatureList.add(purchase.signature.value)
        skuList.add(purchase.product.name)
      }
    } catch (exception: Exception) {
      return createReturnBundle(billingMessagesMapper.mapPurchasesError(exception))
    }

    return createReturnBundle(result.apply {
      putStringArrayList(AppcoinsBillingBinder.INAPP_PURCHASE_ID_LIST, idsList)
      putStringArrayList(AppcoinsBillingBinder.INAPP_PURCHASE_DATA_LIST, dataList)
      putStringArrayList(AppcoinsBillingBinder.INAPP_PURCHASE_ITEM_LIST, skuList)
      putStringArrayList(AppcoinsBillingBinder.INAPP_DATA_SIGNATURE_LIST, signatureList)
      putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
    })
  }

  private fun getSkuDetails(
    apiVersion: Int,
    packageName: String,
    billingType: String?,
    skusBundle: Bundle?
  ): Bundle {
    if (apiVersion != SUPPORTED_API_VERSION) {
      return Bundle().apply {
        putInt(
          AppcoinsBillingBinder.RESPONSE_CODE,
          AppcoinsBillingBinder.RESULT_BILLING_UNAVAILABLE
        )
      }
    }
    val result = Bundle()
    val skus = skusBundle?.getStringArrayList(AppcoinsBillingBinder.ITEM_ID_LIST)

    if (skusBundle == null
      || !skusBundle.containsKey(AppcoinsBillingBinder.ITEM_ID_LIST)
      || billingType.isNullOrBlank()
      || skus.isNullOrEmpty()
    ) {
      with(result) {
        putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_DEVELOPER_ERROR)
      }
      return createReturnBundle(result)
    }

    val type = try {
      BillingSupportedType.valueOfItemType(billingType)
    } catch (e: Exception) {
      with(result) {
        putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_DEVELOPER_ERROR)
      }
      return result
    }

    return try {
      val serializedProducts = billing.getProducts(packageName, skus, type)
        .doOnError { it.printStackTrace() }
        .onErrorResumeNext { Single.error(billingMessagesMapper.mapException(it)) }
        .flatMap { Single.just(serializer.serializeProducts(it)) }
        .subscribeOn(networkScheduler)
        .blockingGet()

      createReturnBundle(billingMessagesMapper.mapSkuDetails(serializedProducts))
    } catch (exception: Exception) {
      exception.printStackTrace()
      createReturnBundle(billingMessagesMapper.mapSkuDetailsError(exception))
    }
  }

  private fun createReturnBundle(bundle: Bundle): Bundle {
    return Bundle().apply {
      putBundle(RESULT_VALUE, bundle)
    }
  }

  private fun isBillingSupported(apiVersion: Int, packageName: String, type: String?): Parcelable {
    val response = Bundle()
    if (apiVersion != SUPPORTED_API_VERSION) {
      with(response) { putInt(RESULT_VALUE, AppcoinsBillingBinder.RESULT_BILLING_UNAVAILABLE) }
      return response
    }
    if (type.isNullOrBlank()) {
      with(response) { putInt(RESULT_VALUE, AppcoinsBillingBinder.RESULT_BILLING_UNAVAILABLE) }
      return response
    }
    val isSupported = when (type) {
      AppcoinsBillingBinder.ITEM_TYPE_INAPP -> billing.isInAppSupported(packageName)
      AppcoinsBillingBinder.ITEM_TYPE_SUBS -> billing.isSubsSupported(packageName)
      else -> Single.just(Billing.BillingSupportType.UNKNOWN_ERROR)
    }.subscribeOn(networkScheduler)
      .map { supported -> billingMessagesMapper.mapSupported(supported) }
      .blockingGet()
    with(response) { putInt(RESULT_VALUE, isSupported) }
    return response
  }
}