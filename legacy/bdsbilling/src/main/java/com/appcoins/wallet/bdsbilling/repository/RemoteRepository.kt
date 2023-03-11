package com.appcoins.wallet.bdsbilling.repository


import com.appcoins.wallet.bdsbilling.SubscriptionsResponse
import com.appcoins.wallet.bdsbilling.merge
import com.appcoins.wallet.bdsbilling.repository.entity.*
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType
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

  interface BrokerBdsApi {

    @GET("8.20180518/transactions")
    fun getSkuTransaction(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Query("cursor") cursor: Long,
      @Query("type") type: BillingSupportedType,
      @Query("limit") limit: Long,
      @Query("sort.name") sort: String,
      @Query("sort.reverse") isReverse: Boolean,
      @Query("product") skuId: String?,
      @Query("domain") packageName: String
    ): Single<TransactionsResponse>

    @GET("8.20180518/transactions/{uId}")
    fun getAppcoinsTransaction(
      @Path("uId") uId: String,
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature")
      walletSignature: String
    ): Single<Transaction>

    /**
     * @param value, value of purchase
     * @param currency, currency of purchase
     * @param currencyType, filter for appc and credits payment, use fiat if you don't want appc and credits
     * @param direct, either if it returns non-direct payments (false) (earn appcoins and ask someone to pay) or not
     * @param type, INAPP, INAPP_UNMANAGED or TOPUP. This is used to filter async payments in INAPP and INAPP_UNMANAGED,
     * if null no filter is applied by transactionType
     *
     */
    @GET("8.20230101/methods")
    fun getPaymentMethods(
      @Query("price.value") value: String? = null,
      @Query("price.currency") currency: String? = null,
      @Query("currency.type") currencyType: String? = null,
      @Query("direct") direct: Boolean? = null,
      @Query("transaction.type") type: String?,
      @Query("domain") packageName: String?
    ): Single<GetMethodsResponse>

    @FormUrlEncoded
    @PATCH("8.20200810/gateways/{gateway}/transactions/{uid}")
    fun patchTransaction(
      @Path("gateway") gateway: String,
      @Path("uid") uid: String,
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Field("pay_key")
      paykey: String
    ): Completable

    /**
     * @param gateway type of the transaction that is being created;
     * @param creditsPurchaseBody CreditsPurchaseBody.
     * @param walletAddress address of the user wallet
     * @param walletSignature signature obtained after signing the wallet
     */
    @POST("8.20200810/gateways/{gateway}/transactions")
    @Headers("Content-Type: application/json; format=product_token")
    fun createTransaction(
      @Path("gateway") gateway: String,
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body creditsPurchaseBody: CreditsPurchaseBody
    ): Single<Transaction>

    /**
     * All optional fields should be passed despite possible being null as these are
     * required by some applications to complete the purchase flow
     * @param gateway type of the transaction that is being created;
     * @see com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status
     * @param origin value from the transaction origin (bds, unity, unknown)
     * @param domain package name of the application
     * @param priceValue amount of the transaction. Only needed in one step payments
     * @param priceCurrency currency of the transaction. Only needed in one step payments
     * @param product name of the product that is being bought
     * @param type name of the payment method being used
     * @param userWallet address of the user wallet
     * @param walletsDeveloper Wallet address of the apps developer
     * @param token
     * @param developerPayload Group of details used in some purchases by the application to
     * complete the purchase
     * @param callback url used in some purchases by the application to complete the purchase
     * @param orderReference reference used in some purchases by the application to
     * @param referrerUrl url to validate the transaction
     * @param walletAddress address of the user wallet
     * @param walletSignature signature obtained after signing the wallet
     */
    @FormUrlEncoded
    @POST("8.20200810/gateways/{gateway}/transactions")
    fun createTransaction(
      @Path("gateway") gateway: String,
      @Field("origin") origin: String?,
      @Field("domain") domain: String,
      @Field("price.value") priceValue: String?,
      @Field("price.currency") priceCurrency: String,
      @Field("product") product: String?,
      @Field("type") type: String,
      @Field("wallets.user") userWallet: String?,
      @Field("wallets.developer") walletsDeveloper: String?,
      @Field("entity.oemid") entityOemId: String?,
      @Field("entity.domain") entityDomain: String?,
      @Field("entity.promo_code") entityPromoCode: String?,
      @Field("token") token: String?,
      @Field("metadata") developerPayload: String?,
      @Field("callback_url") callback: String?,
      @Field("reference") orderReference: String?,
      @Field("referrer_url") referrerUrl: String?,
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String
    ): Single<Transaction>

    /**
     * All optional fields should be passed despite possible being null as these are
     * required by some applications to complete the purchase flow
     * @see com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status
     * @param origin value from the transaction origin (bds, unity, unknown)
     * @param domain package name of the application
     * @param priceValue amount of the transaction. Only needed in one step payments
     * @param priceCurrency currency of the transaction. Only needed in one step payments
     * @param product name of the product that is being bought
     * @param type type of payment being done (inapp, inapp_unmanaged, ...)
     * @param userWallet address of the user wallet
     * @param walletsDeveloper Wallet address of the apps developer
     * @param method payment method used on the gateway
     * @param developerPayload Group of details used in some purchases by the application to
     * complete the purchase
     * @param callback url used in some purchases by the application to complete the purchase
     * @param orderReference reference used in some purchases by the application to
     * @param referrerUrl url to validate the transaction
     * @param walletAddress address of the user wallet
     * @param walletSignature signature obtained after signing the wallet
     */
    @FormUrlEncoded
    @POST("8.20200810/gateways/myappcoins/transactions")
    fun createTransaction(
      @Field("origin") origin: String?,
      @Field("domain") domain: String,
      @Field("price.value") priceValue: String?,
      @Field("price.currency") priceCurrency: String?,
      @Field("product") product: String?,
      @Field("type") type: String,
      @Field("wallets.user") userWallet: String?,
      @Field("wallets.developer") walletsDeveloper: String?,
      @Field("entity.oemid") entityOemId: String?,
      @Field("entity.domain") entityDomain: String?,
      @Field("entity.promo_code") entityPromoCode: String?,
      @Field("method") method: String?,
      @Field("metadata") developerPayload: String?,
      @Field("callback_url") callback: String?,
      @Field("reference") orderReference: String?,
      @Field("referrer_url") referrerUrl: String?,
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String
    ): Single<Transaction>
  }
}
