package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.TransactionsResponse
import com.appcoins.wallet.bdsbilling.repository.entity.*
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

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
                     @Query("wallet.signature")
                     walletSignature: String): Single<InappPurchaseResponse>

  @GET("broker/8.20200101/transactions")
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

  @GET("broker/8.20200101/transactions/{uId}")
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
                      @Body data: RemoteRepository.Consumed): Completable

  /**
   * @param value, value of purchase
   * @param currency, currency of purchase
   * @param type, filter for appc and credits payment, use fiat if you don't want appc and credits
   * @param direct, either if it returns non-direct payments (false) (earn appcoins and ask someone to pay) or not
   * @param transaction.type, INAPP, INAPP_UNMANAGED or TOPUP. This is used to filter async payments in INAPP and INAPP_UNMANAGED,
   * if null no filter is applied by transactionType
   *
   */
  @GET("broker/8.20201101/methods")
  fun getPaymentMethods(@Query("price.value") value: String? = null,
                        @Query("price.currency") currency: String? = null,
                        @Query("currency.type") type: String? = null,
                        @Query("direct") direct: Boolean? = null,
                        @Query("transaction.type") transactionType: String? = null
  ): Single<GetMethodsResponse>

  @FormUrlEncoded
  @PATCH("broker/8.20180518/gateways/{gateway}/transactions/{uid}")
  fun patchTransaction(
      @Path("gateway") gateway: String,
      @Path("uid") uid: String, @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String, @Field("pay_key")
      paykey: String): Completable

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
   * complete the purchase
   */
  @FormUrlEncoded
  @POST("broker/8.20180518/gateways/{gateway}/transactions")
  fun createTransaction(@Path("gateway") gateway: String,
                        @Field("origin") origin: String?,
                        @Field("domain") domain: String,
                        @Field("price.value") priceValue: String?,
                        @Field("price.currency") priceCurrency: String,
                        @Field("product") product: String?,
                        @Field("type") type: String,
                        @Field("wallets.user") userWallet: String?,
                        @Field("wallets.developer") walletsDeveloper: String?,
                        @Field("wallets.store") walletsStore: String?,
                        @Field("wallets.oem") walletsOem: String?,
                        @Field("token") token: String?,
                        @Field("metadata") developerPayload: String?,
                        @Field("callback_url") callback: String?,
                        @Field("reference") orderReference: String?,
                        @Field("referrer_url") referrerUrl: String?,
                        @Query("wallet.address") walletAddress: String,
                        @Query("wallet.signature") walletSignature: String): Single<Transaction>

  /**
   * Overload of createTransaction to receive Body, since only myappcoins receive Body.
   * This is the recommendation from Retrofit when there's a possibility of not having an empty body
   */
  @POST("broker/8.20200810/gateways/myappcoins/transactions")
  fun createTransaction(@Query("origin") origin: String?,
                        @Query("domain") domain: String,
                        @Query("price.value") priceValue: String?,
                        @Query("price.currency") priceCurrency: String,
                        @Query("product") product: String?,
                        @Query("type") type: String,
                        @Query("wallets.user") userWallet: String?,
                        @Query("wallets.developer") walletsDeveloper: String?,
                        @Query("wallets.store") walletsStore: String?,
                        @Query("wallets.oem") walletsOem: String?,
                        @Query("token") token: String?,
                        @Query("metadata") developerPayload: String?,
                        @Query("callback_url") callback: String?,
                        @Query("reference") orderReference: String?,
                        @Query("referrer_url") referrerUrl: String?,
                        @Query("wallet.address") walletAddress: String,
                        @Query("wallet.signature") walletSignature: String,
                        @Body
                        localPaymentBody: RemoteRepository.LocalPaymentBody): Single<Transaction>
}