package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.repository.entity.Product
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.product.InappBillingApi
import com.appcoins.wallet.core.network.microservices.api.product.SubscriptionBillingApi
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType
import com.appcoins.wallet.core.network.microservices.model.DetailsResponseBody
import com.appcoins.wallet.core.network.microservices.model.GetPurchasesResponse
import com.appcoins.wallet.core.network.microservices.model.MiPayTransaction
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.network.microservices.model.SubscriptionsResponse
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.core.network.microservices.model.TransactionsResponse
import com.appcoins.wallet.core.network.microservices.model.merge
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.sharedpreferences.FiatCurrenciesPreferencesDataSource
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.math.BigDecimal
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class RemoteRepository(
  private val brokerBdsApi: BrokerBdsApi,
  private val inappApi: InappBillingApi,
  private val responseMapper: BdsApiResponseMapper,
  private val subsApi: SubscriptionBillingApi,
  private val rxSchedulers: RxSchedulers,
  private val fiatCurrenciesPreferences: FiatCurrenciesPreferencesDataSource,
) {
  companion object {
    private const val SKUS_DETAILS_REQUEST_LIMIT = 50
    private const val SKUS_SUBS_DETAILS_REQUEST_LIMIT = 100
    private const val TOP_UP_TYPE = "TOPUP"
    private const val ANDROID_CHANNEL = "ANDROID"

    class DuplicateException : Exception()

    var executingAppcTransaction = AtomicBoolean(false)
  }

  internal fun isBillingSupported(packageName: String): Single<Boolean> =
    inappApi.getPackage(packageName) // If it's not supported it returns an error that is handle in BdsBilling.kt

  internal fun getSkuDetails(
    packageName: String,
    skus: List<String>
  ): Single<List<Product>> =
    requestSkusDetails(packageName, skus).map { responseMapper.map(it) }

  internal fun getSkuDetailsSubs(
    packageName: String,
    skus: List<String>
  ): Single<List<Product>> =
    requestSkusDetailsSubs(packageName, skus).map { responseMapper.map(it) }

  private fun requestSkusDetails(
    packageName: String,
    skus: List<String>
  ): Single<DetailsResponseBody> = if (skus.size <= SKUS_DETAILS_REQUEST_LIMIT) {
    inappApi.getConsumables(
      packageName = packageName,
      names = skus.joinToString(separator = ","),
      currency = fiatCurrenciesPreferences.getCachedSelectedCurrency(),
    )
  } else {
    Single.zip(
      inappApi.getConsumables(
        packageName = packageName,
        names = skus.take(SKUS_DETAILS_REQUEST_LIMIT).joinToString(separator = ","),
        currency = fiatCurrenciesPreferences.getCachedSelectedCurrency(),
      ),
      requestSkusDetails(
        packageName = packageName,
        skus = skus.drop(SKUS_DETAILS_REQUEST_LIMIT)
      )
    ) { firstResponse, secondResponse -> firstResponse.merge(secondResponse) }
  }

  private fun requestSkusDetailsSubs(
    packageName: String, skus: List<String>
  ): Single<SubscriptionsResponse> = if (skus.size <= SKUS_SUBS_DETAILS_REQUEST_LIMIT) {
    subsApi.getSubscriptions(
      language = Locale.getDefault().toLanguageTag(),
      domain = packageName,
      skus = skus.joinToString(separator = ",")
    )
  } else {
    Single.zip(
      subsApi.getSubscriptions(
        language = Locale.getDefault().toLanguageTag(),
        domain = packageName,
        skus = skus.take(SKUS_SUBS_DETAILS_REQUEST_LIMIT).joinToString(separator = ",")
      ),
      requestSkusDetailsSubs(packageName, skus.drop(SKUS_SUBS_DETAILS_REQUEST_LIMIT))
    ) { firstResponse, secondResponse -> firstResponse.merge(secondResponse) }
  }

  internal fun getSkuPurchase(
    packageName: String,
    skuId: String?
  ): Single<Purchase> = inappApi.getPurchases(
    packageName = packageName,
    type = BillingSupportedType.INAPP.name.lowercase(Locale.ROOT),
    sku = skuId
  ).map {
    if (it.items.isEmpty()) {
      throw HttpException(
        Response.error<GetPurchasesResponse>(
          404,
          "{}".toResponseBody("application/json".toMediaType())
        )
      )
    }
    responseMapper.map(packageName, it)[0]
  }.subscribeOn(rxSchedulers.io)

  internal fun getSkuPurchaseSubs(
    packageName: String,
    purchaseUid: String,
    walletAddress: String,
    walletSignature: String
  ): Single<Purchase> = subsApi.getPurchase(
    domain = packageName,
    uid = purchaseUid,
    walletAddress = walletAddress,
    walletSignature = walletSignature
  ).map { responseMapper.map(packageName, it) }

  internal fun getSkuTransaction(
    packageName: String,
    skuId: String?,
    walletAddress: String,
    walletSignature: String,
    type: BillingSupportedType
  ): Single<TransactionsResponse> = brokerBdsApi.getSkuTransaction(
    walletAddress = walletAddress,
    walletSignature = walletSignature,
    cursor = 0,
    type = type,
    limit = 1,
    sort = "latest",
    isReverse = false,
    skuId = skuId,
    packageName = packageName
  )

  internal fun getPurchases(
    packageName: String,
  ): Single<List<Purchase>> =
    inappApi.getPurchases(
      packageName = packageName,
      type = BillingSupportedType.INAPP.name.lowercase(Locale.ROOT)
    )
      .map { responseMapper.map(packageName, it) }
      .subscribeOn(rxSchedulers.io)

  internal fun getPurchasesSubs(
    packageName: String,
    walletAddress: String,
    walletSignature: String
  ): Single<List<Purchase>> = subsApi.getPurchases(
    domain = packageName,
    walletAddress = walletAddress,
    walletSignature = walletSignature
  ).map { responseMapper.map(packageName, it) }

  @Suppress("unused")
  internal fun acknowledgePurchase(
    packageName: String,
    purchaseToken: String,
  ): Single<Boolean> =
    inappApi.acknowledgePurchase(
      domain = packageName,
      uid = purchaseToken
    )
      .toSingle { true }
      .subscribeOn(rxSchedulers.io)

  internal fun consumePurchase(
    packageName: String,
    purchaseToken: String,
  ): Single<Boolean> =
    inappApi.consumePurchase(
      domain = packageName,
      uid = purchaseToken
    )
      .toSingle { true }
      .subscribeOn(rxSchedulers.io)

  internal fun getSubscriptionToken(
    domain: String,
    skuId: String,
    walletAddress: String,
    walletSignature: String,
    externalBuyerReference: String?,
    isFreeTrial: Boolean?
  ): Single<String> = if (isFreeTrial == true)
    subsApi.getSkuSubscriptionFreeTrialToken(
      domain = domain,
      sku = skuId,
      currency = null,
      walletAddress = walletAddress,
      walletSignature = walletSignature,
      externalBuyerReference = externalBuyerReference,
      isFreeTrial = isFreeTrial
    )
  else
    subsApi.getSkuSubscriptionToken(
      domain = domain,
      sku = skuId,
      currency = null,
      walletAddress = walletAddress,
      walletSignature = walletSignature,
    )

  fun registerAuthorizationProof(
    origin: String?,
    type: String,
    entityOemId: String?,
    entityDomainId: String?,
    id: String?,
    gateway: String,
    walletAddress: String,
    productName: String?,
    packageName: String,
    priceValue: BigDecimal,
    developerPayload: String?,
    callback: String?,
    orderReference: String?,
    referrerUrl: String?,
    guestWalletId: String?
  ): Single<Transaction> = createTransaction(
    userWallet = null,
    entityOemId = entityOemId,
    entityDomain = entityDomainId,
    token = id,
    developerPayload = developerPayload,
    callback = callback,
    orderReference = orderReference,
    referrerUrl = referrerUrl,
    origin = origin,
    type = type,
    gateway = gateway,
    walletAddress = walletAddress,
    packageName = packageName,
    amount = priceValue.toPlainString(),
    currency = "APPC",
    productName = productName,
    guestWalletId = guestWalletId,
  )

  fun registerPaymentProof(
    paymentId: String,
    paymentType: String,
    walletAddress: String,
    paymentProof: String
  ): Completable =
    brokerBdsApi.patchTransaction(
      gateway = paymentType,
      uid = paymentId,
      walletAddress = walletAddress,
      paykey = paymentProof
    ).subscribeOn(rxSchedulers.io)

  internal fun getPaymentMethods(
    value: String?,
    currency: String?,
    currencyType: String?,
    direct: Boolean? = null,
    transactionType: String?,
    packageName: String?,
    entityOemId: String?,
    address: String?,
  ): Single<List<PaymentMethodEntity>> = brokerBdsApi.getPaymentMethods(
    value = value,
    currency = currency,
    currencyType = currencyType,
    direct = direct,
    type = transactionType,
    packageName = packageName,
    darkTheme = transactionType == TOP_UP_TYPE,
    entityOemId = entityOemId,
    walletAddress = address,
    language = Locale.getDefault().language,
    channel = ANDROID_CHANNEL
  ).map { responseMapper.map(it) }

  fun getAppcoinsTransaction(
    uid: String,
    address: String,
    signedContent: String
  ): Single<Transaction> = brokerBdsApi.getAppcoinsTransaction(
    uId = uid,
    walletAddress = address,
    walletSignature = signedContent
  )

  fun transferCredits(
    toWallet: String,
    origin: String,
    type: String,
    gateway: String,
    walletAddress: String,
    packageName: String,
    amount: BigDecimal,
    currency: String,
    guestWalletId: String?
  ): Single<Transaction> = createTransaction(
    userWallet = toWallet,
    entityOemId = null,
    entityDomain = null,
    token = null,
    developerPayload = null,
    callback = null,
    orderReference = null,
    referrerUrl = null,
    origin = origin,
    type = type,
    gateway = gateway,
    walletAddress = walletAddress,
    packageName = packageName,
    amount = amount.toPlainString(),
    currency = currency,
    productName = null,
    guestWalletId = guestWalletId,
  )

  fun createLocalPaymentTransaction(
    paymentId: String,
    packageName: String,
    price: String?,
    currency: String?,
    productName: String?,
    type: String,
    origin: String?,
    entityOemId: String?,
    entityDomain: String?,
    entityPromoCode: String?,
    developerPayload: String?,
    callback: String?,
    orderReference: String?,
    referrerUrl: String?,
    walletAddress: String,
    guestWalletId: String?
  ): Single<Transaction> =
    brokerBdsApi.createTransaction(
      origin = origin,
      domain = packageName,
      priceValue = price,
      priceCurrency = currency,
      product = productName,
      type = type,
      userWallet = walletAddress,
      entityOemId = entityOemId,
      entityDomain = entityDomain,
      entityPromoCode = entityPromoCode,
      method = paymentId,
      developerPayload = developerPayload,
      callback = callback,
      orderReference = orderReference,
      referrerUrl = referrerUrl,
      walletAddress = walletAddress,
      guestWalletId = guestWalletId
    ).subscribeOn(rxSchedulers.io)

  fun createMiPayTransaction(
    paymentId: String,
    packageName: String,
    price: String?,
    currency: String?,
    productName: String?,
    type: String,
    callback: String?,
    referrerUrl: String?,
    walletAddress: String,
    entityOemId: String?,
    returnUrl: String?,
    walletSignature: String?,
    orderReference: String?,
    guestWalletId: String?
  ): Single<MiPayTransaction> =
    brokerBdsApi.createMiPayTransaction(
      domain = packageName,
      priceValue = price,
      priceCurrency = currency,
      product = productName,
      type = type,
      entityOemId = entityOemId,
      method = paymentId,
      callbackUrl = callback,
      referrerUrl = referrerUrl,
      walletAddress = walletAddress,
      walletSignature = walletSignature,
      checkoutUrl = returnUrl,
      orderReference = orderReference,
      guestWalletId = guestWalletId
    ).subscribeOn(rxSchedulers.io)

  fun activateSubscription(
    packageName: String,
    uid: String,
    walletAddress: String,
    walletSignature: String
  ): Completable = subsApi.activateSubscription(
    domain = packageName,
    uid = uid,
    walletAddress = walletAddress,
    walletSignature = walletSignature
  )

  fun cancelSubscription(
    packageName: String,
    uid: String,
    walletAddress: String,
    walletSignature: String
  ): Completable = subsApi.cancelSubscription(
    domain = packageName,
    uid = uid,
    walletAddress = walletAddress,
    walletSignature = walletSignature
  )

  private fun createTransaction(
    userWallet: String?,
    entityOemId: String?,
    entityDomain: String?,
    token: String?,
    developerPayload: String?,
    callback: String?,
    orderReference: String?,
    referrerUrl: String?,
    origin: String?,
    type: String,
    gateway: String,
    walletAddress: String,
    packageName: String,
    amount: String?,
    @Suppress("SameParameterValue") currency: String,
    productName: String?,
    guestWalletId: String?
  ): Single<Transaction> =
    if (executingAppcTransaction.compareAndSet(false, true)) {
      brokerBdsApi.createTransaction(
        gateway = gateway,
        origin = origin,
        domain = packageName,
        priceValue = amount,
        priceCurrency = currency,
        product = productName,
        type = type,
        userWallet = userWallet,
        entityOemId = entityOemId,
        entityDomain = entityDomain,
        entityPromoCode = null,
        token = token,
        developerPayload = developerPayload,
        callback = callback,
        orderReference = orderReference,
        referrerUrl = referrerUrl,
        guestWalletId = guestWalletId,
        walletAddress = walletAddress,
      )
    } else {
      Single.error(DuplicateException())
    }
      .subscribeOn(rxSchedulers.io)
      .doOnSuccess { executingAppcTransaction.set(false) }
      .doOnError { executingAppcTransaction.set(false) }
}
