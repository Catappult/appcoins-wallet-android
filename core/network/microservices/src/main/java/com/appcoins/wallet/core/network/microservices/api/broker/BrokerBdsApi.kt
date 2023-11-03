package com.appcoins.wallet.core.network.microservices.api.broker

import com.appcoins.wallet.core.network.microservices.model.*
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

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
    @Query("wallet.signature") walletSignature: String
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
  @GET("8.20231027/methods")
  fun getPaymentMethods(
    @Query("price.value") value: String? = null,
    @Query("price.currency") currency: String? = null,
    @Query("currency.type") currencyType: String? = null,
    @Query("direct") direct: Boolean? = null,
    @Query("transaction.type") type: String?,
    @Query("domain") packageName: String?,
    @Query("oem_id") entityOemId: String?,
    @Query("wallet.address") walletAddress: String?
  ): Single<GetMethodsResponse>

  @FormUrlEncoded
  @PATCH("8.20200810/gateways/{gateway}/transactions/{uid}")
  fun patchTransaction(
    @Path("gateway") gateway: String,
    @Path("uid") uid: String,
    @Query("wallet.address") walletAddress: String,
    @Header("authorization") authorization: String,
    @Field("pay_key") paykey: String
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
    @Header("authorization") authorization: String,
    @Body creditsPurchaseBody: CreditsPurchaseBody
  ): Single<Transaction>

  /**
   * All optional fields should be passed despite possible being null as these are
   * required by some applications to complete the purchase flow
   * @param gateway type of the transaction that is being created;
   * @see Transaction.Status
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
    @Header("authorization") authorization: String,
  ): Single<Transaction>

  /**
   * All optional fields should be passed despite possible being null as these are
   * required by some applications to complete the purchase flow
   * @see Transaction.Status
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
    @Header("authorization") authorization: String,
  ): Single<Transaction>
}
