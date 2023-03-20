package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.repository.entity.*
import com.appcoins.wallet.core.network.bds.api.BdsApiSecondary
import com.appcoins.wallet.core.network.bds.model.GetWalletResponse
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.product.InappBillingApi
import com.appcoins.wallet.core.network.microservices.api.product.SubscriptionBillingApi
import com.appcoins.wallet.core.network.microservices.model.*
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.*
import java.math.BigDecimal
import java.util.*

class RemoteRepository(
  private val brokerBdsApi: BrokerBdsApi,
  private val inappApi: InappBillingApi,
  private val responseMapper: BdsApiResponseMapper,
  private val bdsApiSecondary: BdsApiSecondary,
  private val subsApi: SubscriptionBillingApi,
) {
  companion object {
    private const val SKUS_DETAILS_REQUEST_LIMIT = 50
    private const val ESKILLS = "ESKILLS"
    private const val SKUS_SUBS_DETAILS_REQUEST_LIMIT = 100
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
          packageName,
          skus.take(SKUS_DETAILS_REQUEST_LIMIT).joinToString(separator = ",")
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
          Locale.getDefault().toLanguageTag(),
          packageName,
          skus.take(SKUS_SUBS_DETAILS_REQUEST_LIMIT)
        ), requestSkusDetailsSubs(packageName, skus.drop(SKUS_SUBS_DETAILS_REQUEST_LIMIT))
      ) { firstResponse, secondResponse -> firstResponse.merge(secondResponse) }
    }

  internal fun getSkuPurchase(
    packageName: String,
    skuId: String?,
    walletAddress: String,
    walletSignature: String
  ): Single<Purchase> =
    inappApi.getPurchases(
      packageName,
      walletAddress,
      walletSignature,
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

  internal fun getSkuPurchaseSubs(
    packageName: String,
    purchaseUid: String,
    walletAddress: String,
    walletSignature: String
  ): Single<Purchase> =
    subsApi.getPurchase(packageName, purchaseUid, walletAddress, walletSignature)
      .map { responseMapper.map(packageName, it) }

  internal fun getSkuTransaction(
    packageName: String,
    skuId: String?,
    walletAddress: String,
    walletSignature: String,
    type: BillingSupportedType
  ): Single<TransactionsResponse> =
    brokerBdsApi.getSkuTransaction(
      walletAddress,
      walletSignature,
      0,
      type,
      1,
      "latest",
      false,
      skuId,
      packageName
    )

  internal fun getPurchases(
    packageName: String,
    walletAddress: String,
    walletSignature: String
  ): Single<List<Purchase>> =
    inappApi.getPurchases(
      packageName,
      walletAddress,
      walletSignature,
      BillingSupportedType.INAPP.name.toLowerCase(Locale.ROOT)
    )
      .map { responseMapper.map(packageName, it) }

  internal fun getPurchasesSubs(
    packageName: String,
    walletAddress: String,
    walletSignature: String
  ): Single<List<Purchase>> =
    subsApi.getPurchases(packageName, walletAddress, walletSignature)
      .map { responseMapper.map(packageName, it) }

  @Suppress("unused")
  internal fun acknowledgePurchase(
    packageName: String, purchaseToken: String,
    walletAddress: String,
    walletSignature: String
  ): Single<Boolean> =
    inappApi.acknowledgePurchase(packageName, purchaseToken, walletAddress, walletSignature)
      .toSingle { true }

  internal fun consumePurchase(
    packageName: String,
    purchaseToken: String,
    walletAddress: String,
    walletSignature: String
  ): Single<Boolean> =
    inappApi.consumePurchase(packageName, purchaseToken, walletAddress, walletSignature)
      .toSingle { true }

  internal fun getSubscriptionToken(
    domain: String,
    skuId: String,
    walletAddress: String,
    walletSignature: String
  ): Single<String> =
    subsApi.getSkuSubscriptionToken(domain, skuId, null, walletAddress, walletSignature)

  fun registerAuthorizationProof(
    origin: String?,
    type: String,
    entityOemId: String?,
    entityDomainId: String?,
    id: String?,
    gateway: String,
    walletAddress: String,
    walletSignature: String,
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
      null,
      developerWallet,
      entityOemId,
      entityDomainId,
      id,
      developerPayload,
      callback,
      orderReference,
      referrerUrl,
      productToken,
      origin,
      type,
      gateway,
      walletAddress,
      walletSignature,
      packageName,
      priceValue.toPlainString(),
      "APPC",
      productName
    )

  fun registerPaymentProof(
    paymentId: String,
    paymentType: String,
    walletAddress: String,
    walletSignature: String,
    paymentProof: String
  ): Completable =
    brokerBdsApi.patchTransaction(
      paymentType,
      paymentId,
      walletAddress,
      walletSignature,
      paymentProof
    )

  internal fun getPaymentMethods(
    value: String?,
    currency: String?,
    currencyType: String?,
    direct: Boolean? = null,
    transactionType: String?,
    packageName: String?
  ): Single<List<PaymentMethodEntity>> =
    brokerBdsApi.getPaymentMethods(
      value,
      currency,
      currencyType,
      direct,
      transactionType,
      packageName
    )
      .map { responseMapper.map(it) }

  fun getAppcoinsTransaction(
    uid: String,
    address: String,
    signedContent: String
  ): Single<Transaction> =
    brokerBdsApi.getAppcoinsTransaction(uid, address, signedContent)

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
  ): Completable =
    createTransaction(
      toWallet,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      origin,
      type,
      gateway,
      walletAddress,
      signature,
      packageName,
      amount.toPlainString(),
      "APPC",
      null
    ).toCompletable()

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
    walletAddress: String,
    walletSignature: String
  ): Single<Transaction> =
    brokerBdsApi.createTransaction(
      origin,
      packageName,
      price,
      currency,
      productName,
      type,
      walletAddress,
      walletsDeveloper,
      entityOemId,
      entityDomain,
      entityPromoCode,
      paymentId,
      developerPayload,
      callback,
      orderReference,
      referrerUrl,
      walletAddress,
      walletSignature
    )

  fun activateSubscription(
    packageName: String,
    uid: String,
    walletAddress: String,
    walletSignature: String
  ): Completable =
    subsApi.activateSubscription(packageName, uid, walletAddress, walletSignature)

  fun cancelSubscription(
    packageName: String,
    uid: String,
    walletAddress: String,
    walletSignature: String
  ): Completable =
    subsApi.cancelSubscription(packageName, uid, walletAddress, walletSignature)

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
    signature: String,
    packageName: String,
    amount: String?,
    @Suppress("SameParameterValue") currency: String,
    productName: String?
  ): Single<Transaction> =
    if (type == ESKILLS) {
      brokerBdsApi.createTransaction(
        gateway,
        walletAddress,
        signature,
        CreditsPurchaseBody(callback, productToken)
      )
    } else {
      brokerBdsApi.createTransaction(
        gateway,
        origin,
        packageName,
        amount,
        currency,
        productName,
        type,
        userWallet,
        developerWallet,
        entityOemId,
        entityDomain,
        null,
        token,
        developerPayload,
        callback,
        orderReference,
        referrerUrl,
        walletAddress,
        signature
      )
    }
}
