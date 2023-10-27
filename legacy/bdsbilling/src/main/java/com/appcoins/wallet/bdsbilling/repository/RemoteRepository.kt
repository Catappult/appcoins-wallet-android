package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.repository.entity.*
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.bds.api.BdsApiSecondary
import com.appcoins.wallet.core.network.bds.model.GetWalletResponse
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.product.InappBillingApi
import com.appcoins.wallet.core.network.microservices.api.product.SubscriptionBillingApi
import com.appcoins.wallet.core.network.microservices.model.*
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.*
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class RemoteRepository(
  private val brokerBdsApi: BrokerBdsApi,
  private val inappApi: InappBillingApi,
  private val responseMapper: BdsApiResponseMapper,
  private val bdsApiSecondary: BdsApiSecondary,
  private val subsApi: SubscriptionBillingApi,
  private val ewtObtainer: EwtAuthenticatorService,
  private val rxSchedulers: RxSchedulers,
) {
  companion object {
    private const val SKUS_DETAILS_REQUEST_LIMIT = 50
    private const val ESKILLS = "ESKILLS"
    private const val SKUS_SUBS_DETAILS_REQUEST_LIMIT = 100
    class DuplicateException(): Exception()
    var executingAppcTransaction = AtomicBoolean(false)
  }

  internal fun isBillingSupported(packageName: String): Single<Boolean> =
    inappApi.getPackage(packageName) // If it's not supported it returns an error that is handle in BdsBilling.kt

  internal fun getSkuDetails(packageName: String, skus: List<String>): Single<List<Product>> =
    requestSkusDetails(packageName, skus).map { responseMapper.map(it) }

  internal fun getSkuDetailsSubs(packageName: String, skus: List<String>): Single<List<Product>> =
    requestSkusDetailsSubs(packageName, skus).map { responseMapper.map(it) }

  private fun requestSkusDetails(
    packageName: String,
    skus: List<String>
  ): Single<DetailsResponseBody> =
    if (skus.size <= SKUS_DETAILS_REQUEST_LIMIT) {
      inappApi.getConsumables(packageName, skus.joinToString(separator = ","))
    } else {
      Single.zip(
        inappApi.getConsumables(
          packageName = packageName,
          names = skus.take(SKUS_DETAILS_REQUEST_LIMIT).joinToString(separator = ",")
        ), requestSkusDetails(packageName, skus.drop(SKUS_DETAILS_REQUEST_LIMIT))
      ) { firstResponse, secondResponse -> firstResponse.merge(secondResponse) }
    }

  private fun requestSkusDetailsSubs(
    packageName: String,
    skus: List<String>
  ): Single<SubscriptionsResponse> =
    if (skus.size <= SKUS_SUBS_DETAILS_REQUEST_LIMIT) {
      subsApi.getSubscriptions(Locale.getDefault().toLanguageTag(), packageName, skus)
    } else {
      Single.zip(
        subsApi.getSubscriptions(
          language = Locale.getDefault().toLanguageTag(),
          domain = packageName,
          skus = skus.take(SKUS_SUBS_DETAILS_REQUEST_LIMIT)
        ), requestSkusDetailsSubs(packageName, skus.drop(SKUS_SUBS_DETAILS_REQUEST_LIMIT))
      ) { firstResponse, secondResponse -> firstResponse.merge(secondResponse) }
    }

  internal fun getSkuPurchase(
    packageName: String,
    skuId: String?
  ): Single<Purchase> =
    ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMap { ewt ->
        inappApi.getPurchases(
          packageName = packageName,
          authorization = ewt,
          type = BillingSupportedType.INAPP.name.toLowerCase(Locale.ROOT),
          sku = skuId
        )
          .map {
            if (it.items.isEmpty()) {
              throw HttpException(
                Response.error<GetPurchasesResponse>(
                  404,
                  ResponseBody.create("application/json".toMediaType(), "{}")
                )
              )
            }
            responseMapper.map(packageName, it)[0]
          }
      }

  internal fun getSkuPurchaseSubs(
    packageName: String,
    purchaseUid: String,
    walletAddress: String,
    walletSignature: String
  ): Single<Purchase> =
    subsApi.getPurchase(
      domain = packageName,
      uid = purchaseUid,
      walletAddress = walletAddress,
      walletSignature = walletSignature
    )
      .map { responseMapper.map(packageName, it) }

  internal fun getSkuTransaction(
    packageName: String,
    skuId: String?,
    walletAddress: String,
    walletSignature: String,
    type: BillingSupportedType
  ): Single<TransactionsResponse> =
    brokerBdsApi.getSkuTransaction(
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
    ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMap { ewt ->
        inappApi.getPurchases(
          packageName = packageName,
          authorization = ewt,
          type = BillingSupportedType.INAPP.name.toLowerCase(Locale.ROOT)
        )
          .map { responseMapper.map(packageName, it) }
      }

  internal fun getPurchasesSubs(
    packageName: String,
    walletAddress: String,
    walletSignature: String
  ): Single<List<Purchase>> =
    subsApi.getPurchases(
      domain = packageName,
      walletAddress = walletAddress,
      walletSignature = walletSignature
    )
      .map { responseMapper.map(packageName, it) }

  @Suppress("unused")
  internal fun acknowledgePurchase(
    packageName: String, purchaseToken: String,
  ): Single<Boolean> =
    ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMap { ewt ->
        inappApi.acknowledgePurchase(
          domain = packageName,
          uid = purchaseToken,
          authorization = ewt
        )
          .toSingle { true }
      }

  internal fun consumePurchase(
    packageName: String,
    purchaseToken: String,
  ): Single<Boolean> =
    ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMap { ewt ->
        inappApi.consumePurchase(
          domain = packageName,
          uid = purchaseToken,
          authorization = ewt
        )
          .toSingle { true }
      }

  internal fun getSubscriptionToken(
    domain: String,
    skuId: String,
    walletAddress: String,
    walletSignature: String
  ): Single<String> =
    subsApi.getSkuSubscriptionToken(
      domain = domain,
      sku = skuId,
      currency = null,
      walletAddress = walletAddress,
      walletSignature = walletSignature
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
    developerWallet: String,
    developerPayload: String?,
    callback: String?,
    orderReference: String?,
    referrerUrl: String?,
    productToken: String?
  ): Single<Transaction> =
    createTransaction(
      userWallet = null,
      developerWallet = developerWallet,
      entityOemId = entityOemId,
      entityDomain = entityDomainId,
      token = id,
      developerPayload = developerPayload,
      callback = callback,
      orderReference = orderReference,
      referrerUrl = referrerUrl,
      productToken = productToken,
      origin = origin,
      type = type,
      gateway = gateway,
      walletAddress = walletAddress,
      packageName = packageName,
      amount = priceValue.toPlainString(),
      currency = "APPC",
      productName = productName
    )

  fun registerPaymentProof(
    paymentId: String,
    paymentType: String,
    walletAddress: String,
    paymentProof: String
  ): Completable =
    ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMapCompletable { ewt ->
        brokerBdsApi.patchTransaction(
          gateway = paymentType,
          uid = paymentId,
          walletAddress = walletAddress,
          authorization = ewt,
          paykey = paymentProof
        )
      }

  internal fun getPaymentMethods(
    value: String?,
    currency: String?,
    currencyType: String?,
    direct: Boolean? = null,
    transactionType: String?,
    packageName: String?,
    entityOemId: String?
  ): Single<List<PaymentMethodEntity>> =
    brokerBdsApi.getPaymentMethods(
      value = value,
      currency = currency,
      currencyType = currencyType,
      direct = direct,
      type = transactionType,
      packageName = packageName,
      entityOemId = entityOemId
    )
      .map { responseMapper.map(it) }

  fun getAppcoinsTransaction(
    uid: String,
    address: String,
    signedContent: String
  ): Single<Transaction> =
    brokerBdsApi.getAppcoinsTransaction(
      uId = uid,
      walletAddress = address,
      walletSignature = signedContent
    )

  var ownerWalletCached: GetWalletResponse? = null
  var packageNameCached: String? = null
  fun getWallet(packageName: String, fromCache: Boolean = true): Single<GetWalletResponse> {
    return if (fromCache && ownerWalletCached != null && packageNameCached == packageName)
      Single.just(ownerWalletCached)
    else
      bdsApiSecondary.getWallet(packageName)
        .doOnSuccess {
          ownerWalletCached = it
          packageNameCached = packageName
        }
  }

  fun transferCredits(
    toWallet: String,
    origin: String,
    type: String,
    gateway: String,
    walletAddress: String,
    signature: String,
    packageName: String,
    amount: BigDecimal
  ): Single<Transaction> =
    createTransaction(
      userWallet = toWallet,
      developerWallet = null,
      entityOemId = null,
      entityDomain = null,
      token = null,
      developerPayload = null,
      callback = null,
      orderReference = null,
      referrerUrl = null,
      productToken = null,
      origin = origin,
      type = type,
      gateway = gateway,
      walletAddress = walletAddress,
      packageName = packageName,
      amount = amount.toPlainString(),
      currency = "APPC",
      productName = null
    )

  fun createLocalPaymentTransaction(
    paymentId: String,
    packageName: String,
    price: String?,
    currency: String?,
    productName: String?,
    type: String,
    origin: String?,
    walletsDeveloper: String?,
    entityOemId: String?,
    entityDomain: String?,
    entityPromoCode: String?,
    developerPayload: String?,
    callback: String?,
    orderReference: String?,
    referrerUrl: String?,
    walletAddress: String
  ): Single<Transaction> =
    ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMap { ewt ->
        brokerBdsApi.createTransaction(
          origin = origin,
          domain = packageName,
          priceValue = price,
          priceCurrency = currency,
          product = productName,
          type = type,
          userWallet = walletAddress,
          walletsDeveloper = walletsDeveloper,
          entityOemId = entityOemId,
          entityDomain = entityDomain,
          entityPromoCode = entityPromoCode,
          method = paymentId,
          developerPayload = developerPayload,
          callback = callback,
          orderReference = orderReference,
          referrerUrl = referrerUrl,
          walletAddress = walletAddress,
          authorization = ewt
        )
      }

  fun activateSubscription(
    packageName: String,
    uid: String,
    walletAddress: String,
    walletSignature: String
  ): Completable =
    subsApi.activateSubscription(
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
  ): Completable =
    subsApi.cancelSubscription(
      domain = packageName,
      uid = uid,
      walletAddress = walletAddress,
      walletSignature = walletSignature
    )

  private fun createTransaction(
    userWallet: String?,
    developerWallet: String?,
    entityOemId: String?,
    entityDomain: String?,
    token: String?,
    developerPayload: String?,
    callback: String?,
    orderReference: String?,
    referrerUrl: String?,
    productToken: String?,
    origin: String?,
    type: String,
    gateway: String,
    walletAddress: String,
    packageName: String,
    amount: String?,
    @Suppress("SameParameterValue") currency: String,
    productName: String?
  ): Single<Transaction> =
    ewtObtainer.getEwtAuthentication().subscribeOn(rxSchedulers.io)
      .flatMap { ewt ->
        if (type == ESKILLS) {
          brokerBdsApi.createTransaction(
            gateway = gateway,
            walletAddress = walletAddress,
            authorization = ewt,
            creditsPurchaseBody = CreditsPurchaseBody(callback, productToken, entityOemId)
          )
        } else {
          if(executingAppcTransaction.compareAndSet(false, true)) {
            brokerBdsApi.createTransaction(
              gateway = gateway,
              origin = origin,
              domain = packageName,
              priceValue = amount,
              priceCurrency = currency,
              product = productName,
              type = type,
              userWallet = userWallet,
              walletsDeveloper = developerWallet,
              entityOemId = entityOemId,
              entityDomain = entityDomain,
              entityPromoCode = null,
              token = token,
              developerPayload = developerPayload,
              callback = callback,
              orderReference = orderReference,
              referrerUrl = referrerUrl,
              walletAddress = walletAddress,
              authorization = ewt
            )
          } else {
            Single.error(DuplicateException())
          }
        }
      }
      .doOnSuccess { executingAppcTransaction.set(false) }
      .doOnError { executingAppcTransaction.set(false) }
}
