package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.BdsApi
import com.appcoins.wallet.bdsbilling.SubscriptionBillingService
import com.appcoins.wallet.bdsbilling.SubscriptionsResponse
import com.appcoins.wallet.bdsbilling.merge
import com.appcoins.wallet.bdsbilling.repository.entity.*
import com.appcoins.wallet.billing.repository.entity.Product
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.util.*

class RemoteRepository(private val inapApi: BdsApi,
                       private val responseMapper: BdsApiResponseMapper,
                       private val bdsApiSecondary: BdsApiSecondary,
                       private val subsApi: SubscriptionBillingService
) {
  companion object {
    private const val SKUS_DETAILS_REQUEST_LIMIT = 50
    private const val SKUS_SUBS_DETAILS_REQUEST_LIMIT = 100
  }

  internal fun isBillingSupported(packageName: String,
                                  type: BillingSupportedType): Single<Boolean> {
    return inapApi.getPackage(packageName, type.name.toLowerCase(Locale.ROOT))
        .map { true } // If it's not supported it returns an error that is handle in BdsBilling.kt
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
      inapApi.getPackages(packageName, skus.joinToString(separator = ","))
    } else {
      Single.zip(
          inapApi.getPackages(packageName,
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
      subsApi.getSubscriptions(packageName, skus)
    } else {
      Single.zip(
          subsApi.getSubscriptions(packageName, skus.take(SKUS_SUBS_DETAILS_REQUEST_LIMIT)),
          requestSkusDetailsSubs(packageName, skus.drop(SKUS_SUBS_DETAILS_REQUEST_LIMIT)),
          BiFunction { firstResponse: SubscriptionsResponse, secondResponse: SubscriptionsResponse ->
            firstResponse.merge(secondResponse)
          })
    }
  }

  internal fun getSkuPurchase(packageName: String, skuId: String?, walletAddress: String,
                              walletSignature: String): Single<Purchase> {
    return inapApi.getSkuPurchase(packageName, skuId, walletAddress, walletSignature)
  }

  internal fun getSkuPurchaseSubs(packageName: String, skuId: String?, walletAddress: String,
                                  walletSignature: String): Single<Purchase> {
    return inapApi.getSkuPurchase(packageName, skuId, walletAddress, walletSignature)
  }

  internal fun getSkuTransaction(packageName: String, skuId: String?, walletAddress: String,
                                 walletSignature: String): Single<TransactionsResponse> {
    return inapApi.getSkuTransaction(walletAddress, walletSignature, 0, TransactionType.INAPP, 1,
        "latest", false, skuId, packageName)
  }

  internal fun getSkuTransactionSubs(packageName: String, skuId: String?, walletAddress: String,
                                     walletSignature: String): Single<TransactionsResponse> {
    return inapApi.getSkuTransaction(walletAddress, walletSignature, 0, TransactionType.INAPP, 1,
        "latest", false, skuId, packageName)
  }

  internal fun getPurchases(packageName: String, walletAddress: String,
                            walletSignature: String): Single<List<Purchase>> {
    return inapApi.getPurchases(packageName, walletAddress, walletSignature,
        BillingSupportedType.INAPP.name.toLowerCase(Locale.ROOT))
        .map { responseMapper.map(it) }
  }

  internal fun getPurchasesSubs(packageName: String): Single<List<Purchase>> {
    return subsApi.getPurchases(packageName)
        .map { responseMapper.map(it) }
  }

  internal fun consumePurchase(packageName: String, purchaseToken: String, walletAddress: String,
                               walletSignature: String): Single<Boolean> {
    return inapApi.consumePurchase(packageName, purchaseToken, walletAddress, walletSignature,
        Consumed())
        .toSingle { true }
  }

  fun registerAuthorizationProof(origin: String?, type: String, oemWallet: String, id: String?,
                                 gateway: String, walletAddress: String, walletSignature: String,
                                 productName: String?, packageName: String, priceValue: BigDecimal,
                                 developerWallet: String, storeWallet: String,
                                 developerPayload: String?, callback: String?,
                                 orderReference: String?,
                                 referrerUrl: String?): Single<Transaction> {
    return inapApi.createTransaction(gateway, origin, packageName, priceValue.toPlainString(),
        "APPC", productName, type, null, developerWallet, storeWallet, oemWallet, id,
        developerPayload, callback, orderReference, referrerUrl, walletAddress, walletSignature)
  }

  fun registerPaymentProof(paymentId: String, paymentType: String, walletAddress: String,
                           walletSignature: String, paymentProof: String): Completable {
    return inapApi.patchTransaction(paymentType, paymentId, walletAddress, walletSignature,
        paymentProof)
  }

  internal fun getPaymentMethods(value: String?,
                                 currency: String?): Single<List<PaymentMethodEntity>> {
    return inapApi.getPaymentMethods(value, currency)
        .map { responseMapper.map(it) }
  }

  internal fun getPaymentMethodsForType(type: String): Single<List<PaymentMethodEntity>> {
    return inapApi.getPaymentMethods(type = type)
        .map { responseMapper.map(it) }
  }

  fun getAppcoinsTransaction(uid: String, address: String,
                             signedContent: String): Single<Transaction> {
    return inapApi.getAppcoinsTransaction(uid, address, signedContent)
  }

  fun getWallet(packageName: String): Single<GetWalletResponse> {
    return bdsApiSecondary.getWallet(packageName)
  }

  fun transferCredits(toWallet: String, origin: String, type: String, gateway: String,
                      walletAddress: String, signature: String, packageName: String,
                      amount: BigDecimal): Completable {
    return inapApi.createTransaction(gateway, origin, packageName, amount.toPlainString(),
        "APPC", null, type, toWallet, null, null,
        null, null, null, null, null, null,
        walletAddress, signature)
        .ignoreElement()
  }

  data class Consumed(val status: String = "CONSUMED")
}
