package com.appcoins.wallet.core.network.microservices.api.product

import com.appcoins.wallet.core.network.microservices.model.DetailsResponseBody
import com.appcoins.wallet.core.network.microservices.model.GetPurchasesResponse
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface InappBillingApi {
  @GET("8.20200701/applications/{domain}/inapp")
  fun getPackage(@Path("domain") packageName: String): Single<Boolean>

  @POST("8.20200701/applications/{domain}/inapp/purchases/{uid}/consume")
  fun consumePurchase(@Path("domain") domain: String,
                      @Path("uid") uid: String,
                      @Query("wallet.address") walletAddress: String,
                      @Query("wallet.signature") walletSignature: String,
                      @Query("payload") payload: String? = null): Completable

  @POST("8.20200701/applications/{domain}/inapp/purchases/{uid}/acknowledge")
  fun acknowledgePurchase(@Path("domain") domain: String,
                          @Path("uid") uid: String,
                          @Query("wallet.address") walletAddress: String,
                          @Query("wallet.signature") walletSignature: String,
                          @Query("payload") payload: String? = null): Completable


  @GET("8.20200701/applications/{packageName}/inapp/consumables")
  fun getConsumables(
    @Path("packageName") packageName: String,
    @Query("skus") names: String
  ): Single<DetailsResponseBody>

  @GET("8.20200701/applications/{packageName}/inapp/consumable/purchases")
  fun getPurchases(
    @Path("packageName") packageName: String,
    @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature") walletSignature: String,
    @Query("type") type: String,
    @Query("state") state: String = "PENDING",
    @Query("sku") sku: String? = null,
  ): Single<GetPurchasesResponse>
}