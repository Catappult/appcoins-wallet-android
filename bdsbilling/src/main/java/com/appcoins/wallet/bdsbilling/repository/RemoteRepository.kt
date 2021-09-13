package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.repository.entity.*
import com.appcoins.wallet.billing.repository.entity.Product
import com.google.gson.annotations.SerializedName
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import retrofit2.http.*
import java.math.BigDecimal
import java.util.*

class RemoteRepository(private val api: BdsApi, private val responseMapper: BdsApiResponseMapper,
                       private val bdsApiSecondary: BdsApiSecondary) {
  companion object {
    private const val SKUS_DETAILS_REQUEST_LIMIT = 50
    private const val ESKILLS = "ESKILLS"
  }

  internal fun isBillingSupported(packageName: String,
                                  type: BillingSupportedType): Single<Boolean> {
    return api.getPackage(packageName, type.name.toLowerCase(Locale.ROOT))
        .map { true } // If it's not supported it returns an error that is handle in BdsBilling.kt
  }

  internal fun getSkuDetails(packageName: String, skus: List<String>): Single<List<Product>> {
    return requestSkusDetails(packageName, skus).map { responseMapper.map(it) }
  }

  private fun requestSkusDetails(packageName: String,
                                 skus: List<String>): Single<DetailsResponseBody> {
    return if (skus.size <= SKUS_DETAILS_REQUEST_LIMIT) {
      api.getPackages(packageName, skus.joinToString(separator = ","))
    } else {
      Single.zip(
          api.getPackages(packageName,
              skus.take(SKUS_DETAILS_REQUEST_LIMIT)
                  .joinToString(separator = ",")),
          requestSkusDetails(packageName, skus.drop(SKUS_DETAILS_REQUEST_LIMIT)),
          BiFunction { firstResponse: DetailsResponseBody, secondResponse: DetailsResponseBody ->
            firstResponse.merge(secondResponse)
          })
    }
  }

  internal fun getSkuPurchase(packageName: String,
                              skuId: String?,
                              walletAddress: String,
                              walletSignature: String): Single<Purchase> {
    return api.getSkuPurchase(packageName, skuId, walletAddress, walletSignature)
  }

  internal fun getSkuTransaction(packageName: String,
                                 skuId: String?,
                                 transactionType: TransactionType,
                                 walletAddress: String,
                                 walletSignature: String): Single<TransactionsResponse> {
    return api.getSkuTransaction(walletAddress, walletSignature, 0, transactionType, 1,
        "latest", false, skuId, packageName)

  }

  internal fun getPurchases(packageName: String,
                            walletAddress: String,
                            walletSignature: String,
                            type: BillingSupportedType): Single<List<Purchase>> {
    return api.getPurchases(packageName, walletAddress, walletSignature,
        type.name.toLowerCase(Locale.ROOT))
        .map { responseMapper.map(it) }
  }

  internal fun consumePurchase(packageName: String,
                               purchaseToken: String,
                               walletAddress: String,
                               walletSignature: String): Single<Boolean> {
    return api.consumePurchase(packageName, purchaseToken, walletAddress, walletSignature,
        Consumed())
        .toSingle { true }
  }

  fun registerAuthorizationProof(origin: String?, type: String, entityOemId: String?,
                                 entityDomainId: String?, id: String?,
                                 gateway: String, walletAddress: String, walletSignature: String,
                                 productName: String?, packageName: String, priceValue: BigDecimal,
                                 developerWallet: String, developerPayload: String?,
                                 callback: String?,
                                 orderReference: String?,
                                 referrerUrl: String?,
                                 productToken: String?): Single<Transaction> {
    return createTransaction(null, developerWallet, entityOemId, entityDomainId, id,
        developerPayload, callback, orderReference, referrerUrl, productToken, origin, type,
        gateway,
        walletAddress, walletSignature, packageName, priceValue.toPlainString(), "APPC",
        productName)
  }

  fun registerPaymentProof(paymentId: String, paymentType: String, walletAddress: String,
                           walletSignature: String,
                           paymentProof: String): Completable {
    return api.patchTransaction(paymentType, paymentId, walletAddress, walletSignature,
        paymentProof)
  }

  internal fun getPaymentMethods(value: String?,
                                 currency: String?,
                                 currencyType: String?,
                                 direct: Boolean? = null,
                                 transactionType: String?): Single<List<PaymentMethodEntity>> {
    return api.getPaymentMethods(value, currency, currencyType, direct, transactionType)
        .map { responseMapper.map(it) }
  }

  fun getAppcoinsTransaction(uid: String, address: String,
                             signedContent: String): Single<Transaction> {
    return api.getAppcoinsTransaction(uid, address, signedContent)
  }

  fun getWallet(packageName: String): Single<GetWalletResponse> {
    return bdsApiSecondary.getWallet(packageName)
  }

  fun transferCredits(toWallet: String, origin: String, type: String, gateway: String,
                      walletAddress: String, signature: String, packageName: String,
                      amount: BigDecimal): Completable {
    return createTransaction(toWallet, null, null, null, null, null, null, null, null, null, origin,
        type, gateway, walletAddress, signature, packageName, amount.toPlainString(),
        "APPC",
        null).ignoreElement()
  }

  fun createLocalPaymentTransaction(paymentId: String, packageName: String, price: String?,
                                    currency: String?, productName: String?, type: String,
                                    origin: String?, walletsDeveloper: String?,
                                    entityOemId: String?, entityDomain: String?,
                                    developerPayload: String?, callback: String?,
                                    orderReference: String?, referrerUrl: String?,
                                    walletAddress: String,
                                    walletSignature: String): Single<Transaction> {
    return api.createTransaction(origin, packageName, price, currency, productName, type,
        walletAddress, walletsDeveloper, entityOemId, entityDomain, paymentId, developerPayload,
        callback,
        orderReference, referrerUrl, walletAddress, walletSignature)
  }

  private fun createTransaction(userWallet: String?, developerWallet: String?, entityOemId: String?,
                                entityDomain: String?, token: String?, developerPayload: String?,
                                callback: String?, orderReference: String?, referrerUrl: String?,
                                productToken: String?, origin: String?, type: String,
                                gateway: String, walletAddress: String, signature: String,
                                packageName: String, amount: String?,
                                currency: String,
                                productName: String?): Single<Transaction> {
    if (type == ESKILLS) {
      val creditsPurchaseBody =
          CreditsPurchaseBody(callback, productToken)

      return api.createTransaction(gateway, creditsPurchaseBody, walletAddress, signature)
    } else {
      return api.createTransaction(
          gateway, origin, packageName, amount, currency, productName,
          type, userWallet, developerWallet, entityOemId, entityDomain, token, developerPayload,
          callback, orderReference, referrerUrl, walletAddress, signature
      )
    }
  }

  interface BdsApi {

    @GET("inapp/8.20180518/packages/{packageName}")
    fun getPackage(@Path("packageName") packageName: String, @Query("type")
    type: String): Single<GetPackageResponse>

    @GET("inapp/8.20180518/packages/{packageName}/products")
    fun getPackages(@Path("packageName") packageName: String,
                    @Query("names") names: String): Single<DetailsResponseBody>

    @GET("inapp/8.20180518/packages/{packageName}/products/{skuId}/purchase")
    fun getSkuPurchase(@Path("packageName") packageName: String,
                       @Path("skuId") skuId: String?,
                       @Query("wallet.address") walletAddress: String,
                       @Query("wallet.signature") walletSignature: String): Single<Purchase>

    @GET("broker/8.20180518/transactions")
    fun getSkuTransaction(
        @Query("wallet.address") walletAddress: String,
        @Query("wallet.signature") walletSignature: String,
        @Query("cursor") cursor: Long,
        @Query("type") type: TransactionType,
        @Query("limit") limit: Long,
        @Query("sort.name") sort: String,
        @Query("sort.reverse") isReverse: Boolean,
        @Query("product") skuId: String?,
        @Query("domain") packageName: String
    ): Single<TransactionsResponse>

    @GET("broker/8.20180518/transactions/{uId}")
    fun getAppcoinsTransaction(@Path("uId") uId: String,
                               @Query("wallet.address") walletAddress: String,
                               @Query("wallet.signature")
                               walletSignature: String): Single<Transaction>


    @GET("inapp/8.20180518/packages/{packageName}/purchases")
    fun getPurchases(@Path("packageName") packageName: String,
                     @Query("wallet.address") walletAddress: String,
                     @Query("wallet.signature") walletSignature: String,
                     @Query("type") type: String): Single<GetPurchasesResponse>

    @Headers("Content-Type: application/json")
    @PATCH("inapp/8.20180518/packages/{packageName}/purchases/{purchaseId}")
    fun consumePurchase(@Path("packageName") packageName: String,
                        @Path("purchaseId") purchaseToken: String,
                        @Query("wallet.address") walletAddress: String,
                        @Query("wallet.signature") walletSignature: String,
                        @Body data: Consumed): Completable

    /**
     * @param value, value of purchase
     * @param currency, currency of purchase
     * @param currencyType, filter for appc and credits payment, use fiat if you don't want appc and credits
     * @param direct, either if it returns non-direct payments (false) (earn appcoins and ask someone to pay) or not
     * @param transaction.type, INAPP, INAPP_UNMANAGED or TOPUP. This is used to filter async payments in INAPP and INAPP_UNMANAGED,
     * if null no filter is applied by transactionType
     *
     */
    @GET("broker/8.20210208/methods")
    fun getPaymentMethods(@Query("price.value") value: String? = null,
                          @Query("price.currency") currency: String? = null,
                          @Query("currency.type") currencyType: String? = null,
                          @Query("direct") direct: Boolean? = null,
                          @Query("transaction.type") type: String?): Single<GetMethodsResponse>

    @FormUrlEncoded
    @PATCH("broker/8.20200810/gateways/{gateway}/transactions/{uid}")
    fun patchTransaction(
        @Path("gateway") gateway: String,
        @Path("uid") uid: String, @Query("wallet.address") walletAddress: String,
        @Query("wallet.signature") walletSignature: String, @Field("pay_key")
        paykey: String): Completable

    /**
     * @param gateway type of the transaction that is being created;
     * @param creditsPurchaseBody CreditsPurchaseBody.
     * @param walletAddress address of the user wallet
     * @param walletSignature signature obtained after signing the wallet
     */
    @POST("broker/8.20200810/gateways/{gateway}/transactions")
    @Headers("Content-Type: application/json; format=product_token")
    fun createTransaction(@Path("gateway") gateway: String,
                          @Body creditsPurchaseBody: CreditsPurchaseBody,
                          @Query("wallet.address") walletAddress: String,
                          @Query("wallet.signature") walletSignature: String): Single<Transaction>

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
     * @param walletsOem Wallet address of the original equipment manufacturer
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
    @POST("broker/8.20200810/gateways/{gateway}/transactions")
    fun createTransaction(@Path("gateway") gateway: String,
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
                          @Field("token") token: String?,
                          @Field("metadata") developerPayload: String?,
                          @Field("callback_url") callback: String?,
                          @Field("reference") orderReference: String?,
                          @Field("referrer_url") referrerUrl: String?,
                          @Query("wallet.address") walletAddress: String,
                          @Query("wallet.signature") walletSignature: String): Single<Transaction>

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
     * @param walletsOem Wallet address of the original equipment manufacturer
     * @param walletsStore Wallet address of the store responsible for the app's installation
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
    @POST("broker/8.20200810/gateways/myappcoins/transactions")
    fun createTransaction(@Field("origin") origin: String?,
                          @Field("domain") domain: String,
                          @Field("price.value") priceValue: String?,
                          @Field("price.currency") priceCurrency: String?,
                          @Field("product") product: String?,
                          @Field("type") type: String,
                          @Field("wallets.user") userWallet: String?,
                          @Field("wallets.developer") walletsDeveloper: String?,
                          @Field("entity.oemid") entityOemId: String?,
                          @Field("entity.domain") entityDomain: String?,
                          @Field("method") method: String?,
                          @Field("metadata") developerPayload: String?,
                          @Field("callback_url") callback: String?,
                          @Field("reference") orderReference: String?,
                          @Field("referrer_url") referrerUrl: String?,
                          @Query("wallet.address") walletAddress: String,
                          @Query("wallet.signature") walletSignature: String): Single<Transaction>
  }

  data class Consumed(val status: String = "CONSUMED")

  data class LocalPaymentBody(@SerializedName("price.value") val price: String?,
                              @SerializedName("price.currency") val currency: String?,
                              val domain: String, val type: String, val method: String,
                              val product: String?,
                              @SerializedName("wallets.developer") val developerWallet: String?)
}
