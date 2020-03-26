package com.asfoundation.wallet.subscriptions

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface SubscriptionServiceMocked {

  @GET("product/8.20200301/inapp/{domain}/subscriptions")
  fun getSubscriptions(@Path("domain") domain: String,
                       @Query("cursor") cursor: Long?,
                       @Query("limit") limit: Long?,
                       @Query("skus") type: List<String>?): Single<SubscriptionsResponse>

  @GET("product/8.20200301/inapp/{domain}/subscriptions/{sku}")
  fun getSkuSubscription(@Path("domain") domain: String,
                         @Path("sku") sku: String): Single<SubscriptionResponse>

  @GET("product/8.20200301/inapp/{domain}/subscriptions/{sku}/token")
  fun getSkuSubscriptionToken(@Path("domain") domain: String,
                              @Path("sku") sku: String,
                              @Query("currency") currency: String?): Single<String>

  @GET("product/8.20200301/inapp/{domain}/subscriptions/purchases")
  fun getPurchases(@Path("domain") domain: String,
                   @Query("cursor") cursor: Long?,
                   @Query("limit") limit: Long?): Single<List<SubscriptionsPurchaseResponse>>

  @GET("product/8.20200301/inapp/{domain}/subscriptions/purchases/{uid}")
  fun getPurchase(@Path("domain") domain: String,
                  @Path("uid") uid: String): Single<Purchase>

  @PATCH("product/8.20200301/inapp/{domain}/subscriptions/purchases/{uid}")
  fun updatePurchase(@Path("domain") domain: String,
                     @Path("uid") uid: String,
                     @Body purchaseUpdate: PurchaseUpdate): Completable

}
