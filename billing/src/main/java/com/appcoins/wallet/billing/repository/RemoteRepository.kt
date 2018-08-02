package com.appcoins.wallet.billing.repository

import com.appcoins.wallet.billing.repository.entity.*
import io.reactivex.Single
import retrofit2.http.*

class RemoteRepository(private val api: BdsApi, val responseMapper: BdsApiResponseMapper) {
  companion object {
    const val BASE_HOST = "http://api-dev.blockchainds.com"//BuildConfig.BASE_HOST

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
    return api.consumePurchase(packageName, purchaseToken, walletAddress, walletSignature, Consumed())
        .map { responseMapper.map(it) }
  }

  fun registerAuthorizationProof(id: String, paymentType: String, walletAddress: String,
                                 walletSignature: String, productName: String, packageName: String,
                                 developerWallet: String,
                                 storeWallet: String): Single<RegisterAuthorizationResponse> {
    return api.registerAuthorization(paymentType, walletAddress, walletSignature,
        RegisterAuthorizationBody(productName, packageName, id, developerWallet, storeWallet))
  }

  fun registerPaymentProof(paymentId: String, paymentType: String, walletAddress: String,
                           walletSignature: String,
                           paymentProof: String): Single<PaymentProofResponse> {
    return api.registerPayment(paymentType, paymentId, walletAddress, walletSignature,
        RegisterPaymentBody(paymentProof)).map { PaymentProofResponse() }
  }

  interface BdsApi {

    @GET("inapp/8.20180518/packages/{packageName}")
    fun getPackage(@Path("packageName") packageName: String, @Query("type")
    type: String): Single<GetPackageResponse>

    @GET("inapp/8.20180518/packages/{packageName}/products")
    fun getPackages(@Path("packageName") packageName: String,
                    @Query("names") names: String): Single<DetailsResponseBody>

    @GET("inapp/8.20180518/packages/{packageName}/purchases")
    fun getPurchases(@Path("packageName") packageName: String,
                     @Query("wallet.address") walletAddress: String,
                     @Query("wallet.signature") walletSignature: String,
                     @Query("type") type: String): Single<GetPurchasesResponse>

    @PATCH("inapp/8.20180518/packages/{packageName}/purchases/{uid}")
    fun consumePurchase(@Path("packageName") packageName: String,
                        @Path("uid") purchaseToken: String,
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
                        @Body body: RegisterPaymentBody): Single<Void>


  }

  data class Consumed(val status: String = "CONSUMED")

}
