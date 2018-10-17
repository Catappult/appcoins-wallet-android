package com.appcoins.wallet.billing.repository

import com.appcoins.wallet.billing.BuildConfig
import com.appcoins.wallet.billing.repository.entity.*
import com.appcoins.wallet.billing.repository.entity.authorization.Authorization
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.*
import java.lang.StringBuilder
import java.math.BigDecimal
import java.util.*

class RemoteRepository(private val api: BdsApi, val responseMapper: BdsApiResponseMapper) {
  companion object {
    const val BASE_HOST = BuildConfig.BASE_HOST
  }

  internal fun isBillingSupported(packageName: String,
                                  type: BillingSupportedType): Single<Boolean> {
    return api.getPackage(packageName, type.name.toLowerCase()).map { responseMapper.map(it) }
  }

  internal fun getSkuDetails(packageName: String, skus: List<String>,
                             type: String): Single<List<Product>> {
    return api.getPackages(packageName, skus.joinToString(separator = ","))
        .map { responseMapper.map(it) }
  }

  internal fun getSkuPurchase(packageName: String,
                              skuId: String,
                              walletAddress: String,
                              walletSignature: String): Single<Purchase> {
    return api.getSkuPurchase(packageName, skuId, walletAddress, walletSignature)
  }

  internal fun getSkuTransaction(packageName: String,
                                 skuId: String,
                                 walletAddress: String,
                                 walletSignature: String): Single<Transaction> {
    return api.getSkuTransaction(packageName, skuId, walletAddress, walletSignature)
  }

  internal fun getPurchases(packageName: String,
                            walletAddress: String,
                            walletSignature: String,
                            type: BillingSupportedType): Single<List<Purchase>> {
    return api.getPurchases(packageName, walletAddress, walletSignature,
        type.name.toLowerCase()).map { responseMapper.map(it) }
  }

  internal fun consumePurchase(packageName: String,
                               purchaseToken: String,
                               walletAddress: String,
                               walletSignature: String): Single<Boolean> {
    return api.consumePurchase(packageName, purchaseToken, walletAddress, walletSignature,
        Consumed())
        .map { responseMapper.map(it) }
  }

  fun registerAuthorizationProof(id: String, paymentType: String, walletAddress: String,
                                 walletSignature: String, productName: String, packageName: String,
                                 developerWallet: String, storeWallet: String,
                                 developerPayload: String?): Single<RegisterAuthorizationResponse> {
    return api.registerAuthorization(paymentType, walletAddress, walletSignature,
        RegisterAuthorizationBody(productName, packageName, id, developerWallet, storeWallet,
            developerPayload))
  }

  fun registerPaymentProof(paymentId: String, paymentType: String, walletAddress: String,
                           walletSignature: String,
                           paymentProof: String): Single<PaymentProofResponse> {
    return api.registerPayment(paymentType, paymentId, walletAddress, walletSignature,
        RegisterPaymentBody(paymentProof)).andThen(Single.just(PaymentProofResponse()))
  }

  internal fun getGateways(): Single<List<Gateway>> {
    return api.getGateways().map { responseMapper.map(it) }
  }

  fun patchTransaction(uid: String, walletAddress: String, walletSignature: String,
                       paykey: String): Completable {
    return api.patchTransaction(uid, walletAddress, walletSignature, paykey)
        .ignoreElements()
  }

  fun getSessionKey(uid: String, walletAddress: String,
                    walletSignature: String): Single<Authorization> {
    return api.getSessionKey(uid, walletAddress, walletSignature)
        .singleOrError()
  }

  fun createAdyenTransaction(origin: String?, walletAddress: String,
                             walletSignature: String, token: String,
                             packageName: String, priceValue: BigDecimal, priceCurrency: String, productName: String?, type: String,
                             walletDeveloper: String,
                             walletStore: String, walletOem: String): Single<TransactionStatus> {
    return api.createAdyenTransaction(origin, packageName, priceValue.toString(), priceCurrency, productName, type, walletDeveloper, walletStore, walletOem, token, walletAddress, walletSignature)
            .singleOrError()
  }

  fun getAppcoinsTransaction(uid: String, address: String,
                             signedContent: String): Single<Transaction> {
    return api.getAppcoinsTransaction(uid, address, signedContent)
  }

  interface BdsApi {

    @GET("inapp/8.20180518/packages/{packageName}")
    fun getPackage(@Path("packageName") packageName: String, @Query("type")
    type: String): Single<GetPackageResponse>

    @GET("inapp/8.20180518/packages/{packageName}/products")
    fun getPackages(@Path("packageName") packageName: String,
                    @Query("names") names: String): Single<DetailsResponseBody>

    @Headers("Content-Type: application/json")
    @GET("inapp/8.20180518/packages/{packageName}/products/{skuId}/purchase")
    fun getSkuPurchase(@Path("packageName") packageName: String,
                       @Path("skuId") skuId: String,
                       @Query("wallet.address") walletAddress: String,
                       @Query("wallet.signature") walletSignature: String): Single<Purchase>

    @GET("inapp/8.20180518/packages/{packageName}/products/{skuId}/transaction")
    fun getSkuTransaction(@Path("packageName") packageName: String,
                          @Path("skuId") skuId: String,
                          @Query("wallet.address") walletAddress: String,
                          @Query("wallet.signature") walletSignature: String): Single<Transaction>

    @GET("inapp/8.20180727/gateways/appcoins/transactions/{uId}")
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
                        @Body data: Consumed): Single<Void>

    @Headers("Content-Type: application/json")
    @POST("inapp/8.20180727/gateways/{name}/transactions")
    fun registerAuthorization(@Path("name") gateway: String, @Query("wallet.address")
    walletAddress: String, @Query("wallet.signature") walletSignature: String, @Body
                              body: RegisterAuthorizationBody): Single<RegisterAuthorizationResponse>

    @Headers("Content-Type: application/json")
    @PATCH("inapp/8.20180727/gateways/{gateway}/transactions/{paymentId}")
    fun registerPayment(@Path("gateway") gateway: String,
                        @Path("paymentId") paymentId: String,
                        @Query("wallet.address") walletAddress: String,
                        @Query("wallet.signature") walletSignature: String,
                        @Body body: RegisterPaymentBody): Completable

    @GET("inapp/8.20180518/gateways")
    fun getGateways(): Single<GetGatewaysResponse>

    @FormUrlEncoded
    @PATCH("broker/8.20180518/gateways/adyen/transactions/{uid}")
    fun patchTransaction(
            @Path("uid") uid: String, @Query("wallet.address") walletAddress: String,
            @Query("wallet.signature") walletSignature: String, @Field("pay_key")
        paykey: String): Observable<Any>

    @GET("broker/8.20180518/gateways/adyen/transactions/{uid}/authorization")
    fun getSessionKey(
        @Path("uid") uid: String, @Query("wallet.address") walletAddress: String,
        @Query("wallet.signature") walletSignature: String): Observable<Authorization>

    @FormUrlEncoded
    @POST("broker/8.20180518/gateways/adyen/transactions")
    fun createAdyenTransaction(@Field("origin") origin: String?,
                               @Field("domain") domain: String,
                               @Field("price.value") priceValue: String,
                               @Field("price.currency") priceCurrency: String,
                               @Field("product") product: String?,
                               @Field("type") type: String,
                               @Field("wallets.developer") walletsDeveloper: String,
                               @Field("wallets.store") walletsStore: String,
                               @Field("wallets.oem") walletsOem: String,
                               @Field("token") token: String,
                               @Query("wallet.address") walletAddress: String,
                               @Query("wallet.signature") walletSignature: String): Observable<TransactionStatus>
  }

  data class Consumed(val status: String = "CONSUMED")
}
