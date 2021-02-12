package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.BdsApi
import com.appcoins.wallet.bdsbilling.SubscriptionBillingApi
import com.appcoins.wallet.bdsbilling.SubscriptionsResponse
import com.appcoins.wallet.bdsbilling.merge
import com.appcoins.wallet.bdsbilling.repository.entity.*
import com.google.gson.annotations.SerializedName
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.util.*

class RemoteRepository(private val inAppApi: BdsApi,
                       private val responseMapper: BdsApiResponseMapper,
                       private val bdsApiSecondary: BdsApiSecondary,
                       private val subsApi: SubscriptionBillingApi
) {
  companion object {
    private const val SKUS_DETAILS_REQUEST_LIMIT = 50
    private const val SKUS_SUBS_DETAILS_REQUEST_LIMIT = 100
  }

  internal fun isBillingSupported(packageName: String): Single<Boolean> {
    return inAppApi.getPackage(packageName,
        BillingSupportedType.INAPP.name.toLowerCase(Locale.ROOT))
        .map { true } // If it's not supported it returns an error that is handle in BdsBilling.kt
  }

  internal fun isBillingSupportedSubs(packageName: String): Single<Boolean> {
    return subsApi.getPackage(
        packageName) // If it's not supported it returns an error that is handle in BdsBilling.kt
  }

  internal fun getSkuDetails(packageName: String, skus: List<String>): Single<List<Product>> {
    return requestSkusDetails(packageName, skus).map { responseMapper.map(it) }
  }

  internal fun getSkuDetailsSubs(packageName: String, skus: List<String>): Single<List<Product>> {
    return requestSkusDetailsSubs(packageName, skus).map { responseMapper.map(it) }
  }

  private fun requestSkusDetails(packageName: String,
                                 skus: List<String>): Single<DetailsResponseBody> {
    return if (skus.size <= SKUS_DETAILS_REQUEST_LIMIT) {
      inAppApi.getPackages(packageName, skus.joinToString(separator = ","))
    } else {
      Single.zip(
          inAppApi.getPackages(packageName,
              skus.take(SKUS_DETAILS_REQUEST_LIMIT)
                  .joinToString(separator = ",")),
          requestSkusDetails(packageName, skus.drop(SKUS_DETAILS_REQUEST_LIMIT)),
          BiFunction { firstResponse: DetailsResponseBody, secondResponse: DetailsResponseBody ->
            firstResponse.merge(secondResponse)
          })
    }
  }

  private fun requestSkusDetailsSubs(packageName: String,
                                     skus: List<String>): Single<SubscriptionsResponse> {
    return if (skus.size <= SKUS_SUBS_DETAILS_REQUEST_LIMIT) {
      subsApi.getSubscriptions(Locale.getDefault()
          .toLanguageTag(), packageName, skus)
    } else {
      Single.zip(
          subsApi.getSubscriptions(Locale.getDefault()
              .toLanguageTag(), packageName, skus.take(SKUS_SUBS_DETAILS_REQUEST_LIMIT)),
          requestSkusDetailsSubs(packageName, skus.drop(SKUS_SUBS_DETAILS_REQUEST_LIMIT)),
          BiFunction { firstResponse: SubscriptionsResponse, secondResponse: SubscriptionsResponse ->
            firstResponse.merge(secondResponse)
          })
    }
  }

  internal fun getSkuPurchase(packageName: String, skuId: String?, walletAddress: String,
                              walletSignature: String): Single<Purchase> {
    return inAppApi.getSkuPurchase(packageName, skuId, walletAddress, walletSignature)
        .map { responseMapper.map(packageName, it) }
  }

  internal fun getSkuPurchaseSubs(packageName: String, purchaseUid: String, walletAddress: String,
                                  walletSignature: String): Single<Purchase> {
    return subsApi.getPurchase(packageName, purchaseUid, walletAddress, walletSignature)
        .map { responseMapper.map(packageName, it) }
  }

  internal fun getSkuTransaction(packageName: String, skuId: String?, walletAddress: String,
                                 walletSignature: String,
                                 type: BillingSupportedType): Single<TransactionsResponse> {
    return inAppApi.getSkuTransaction(walletAddress, walletSignature, 0, type, 1,
        "latest", false, skuId, packageName)
  }

  internal fun getPurchases(packageName: String, walletAddress: String,
                            walletSignature: String): Single<List<Purchase>> {
    return inAppApi.getPurchases(packageName, walletAddress, walletSignature,
        BillingSupportedType.INAPP.name.toLowerCase(Locale.ROOT))
        .map { responseMapper.map(packageName, it) }
  }

  internal fun getPurchasesSubs(packageName: String, walletAddress: String,
                                walletSignature: String): Single<List<Purchase>> {
    return subsApi.getPurchases(packageName, walletAddress, walletSignature)
        .map { responseMapper.map(packageName, it) }
  }

  internal fun consumePurchase(packageName: String, purchaseToken: String, walletAddress: String,
                               walletSignature: String): Single<Boolean> {
    return inAppApi.consumePurchase(packageName, purchaseToken, walletAddress, walletSignature,
        Consumed())
        .toSingle { true }
  }

  internal fun acknowledgePurchase(packageName: String, purchaseToken: String,
                                   walletAddress: String,
                                   walletSignature: String): Single<Boolean> {
    return subsApi.acknowledgePurchase(packageName, purchaseToken, walletAddress, walletSignature)
        .toSingle { true }
  }

  internal fun consumePurchaseSubs(packageName: String, purchaseToken: String,
                                   walletAddress: String,
                                   walletSignature: String): Single<Boolean> {
    return subsApi.consumePurchase(packageName, purchaseToken, walletAddress, walletSignature)
        .toSingle { true }
  }

  fun registerAuthorizationProof(origin: String?, type: String, oemWallet: String, id: String?,
                                 gateway: String, walletAddress: String, walletSignature: String,
                                 productName: String?, packageName: String, priceValue: BigDecimal,
                                 developerWallet: String, storeWallet: String,
                                 developerPayload: String?, callback: String?,
                                 orderReference: String?,
                                 referrerUrl: String?): Single<Transaction> {
    return createTransaction(null, developerWallet, storeWallet, oemWallet, id, developerPayload,
        callback, orderReference, referrerUrl, origin, type, gateway, walletAddress,
        walletSignature, packageName, priceValue.toPlainString(), "APPC", productName)
  }

  fun registerPaymentProof(paymentId: String, paymentType: String, walletAddress: String,
                           walletSignature: String, paymentProof: String): Completable {
    return inAppApi.patchTransaction(paymentType, paymentId, walletAddress, walletSignature,
        paymentProof)
  }

  internal fun getPaymentMethods(value: String?,
                                 currency: String?,
                                 currencyType: String?,
                                 direct: Boolean? = null,
                                 transactionType: String?): Single<List<PaymentMethodEntity>> {
    return inAppApi.getPaymentMethods(value, currency, currencyType, direct, transactionType)
        .map { responseMapper.map(it) }
  }

  fun getAppcoinsTransaction(uid: String, address: String,
                             signedContent: String): Single<Transaction> {
    return inAppApi.getAppcoinsTransaction(uid, address, signedContent)
  }

  fun getWallet(packageName: String): Single<GetWalletResponse> {
    return bdsApiSecondary.getWallet(packageName)
  }

  fun transferCredits(toWallet: String, origin: String, type: String, gateway: String,
                      walletAddress: String, signature: String, packageName: String,
                      amount: BigDecimal): Completable {
    return createTransaction(toWallet, null, null, null, null, null, null, null, null, origin, type,
        gateway, walletAddress, signature, packageName, amount.toPlainString(), "APPC",
        null).ignoreElement()
  }

  fun createLocalPaymentTransaction(paymentId: String, packageName: String, price: String?,
                                    currency: String?, productName: String?, type: String,
                                    origin: String?, walletsDeveloper: String?,
                                    walletsStore: String?, walletsOem: String?,
                                    developerPayload: String?, callback: String?,
                                    orderReference: String?, referrerUrl: String?,
                                    walletAddress: String,
                                    walletSignature: String): Single<Transaction> {
    return inAppApi.createTransaction(origin, packageName, price, currency, productName, type,
        walletAddress, walletsDeveloper, walletsStore, walletsOem, null, developerPayload, callback,
        orderReference, referrerUrl, walletAddress, walletSignature,
        LocalPaymentBody(price, currency, packageName, type, paymentId, productName,
            walletsDeveloper))
  }

  fun activateSubscription(packageName: String, uid: String, walletAddress: String,
                           walletSignature: String): Completable {
    return subsApi.activateSubscription(packageName, uid, walletAddress, walletSignature)
  }

  fun cancelSubscription(packageName: String, uid: String, walletAddress: String,
                         walletSignature: String): Completable {
    return subsApi.cancelSubscription(packageName, uid, walletAddress, walletSignature)
  }

  private fun createTransaction(userWallet: String?, developerWallet: String?, storeWallet: String?,
                                oemWallet: String?, token: String?, developerPayload: String?,
                                callback: String?, orderReference: String?, referrerUrl: String?,
                                origin: String?, type: String, gateway: String,
                                walletAddress: String, signature: String, packageName: String,
                                amount: String?, currency: String,
                                productName: String?): Single<Transaction> {
    return inAppApi.createTransaction(gateway, origin, packageName, amount, currency, productName,
        type, userWallet, developerWallet, storeWallet, oemWallet, token, developerPayload,
        callback, orderReference, referrerUrl, walletAddress, signature)
  }

  data class Consumed(val status: String = "CONSUMED")

  data class LocalPaymentBody(@SerializedName("price.value") val price: String?,
                              @SerializedName("price.currency") val currency: String?,
                              val domain: String, val type: String, val method: String,
                              val product: String?,
                              @SerializedName("wallets.developer") val developerWallet: String?)
}
