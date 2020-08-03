package com.appcoins.wallet.bdsbilling

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface SubscriptionBillingApi {

  @GET("product/8.20200701/applications/{domain}/inapp")
  fun getPackage(@Path("domain") packageName: String): Single<Boolean>

  @GET("product/8.20200701/applications/{domain}/inapp/subscriptions")
  fun getSubscriptions(@Path("domain") domain: String,
                       @Query("skus") skus: List<String>?,
                       @Query("limit") limit: Long? = null): Single<SubscriptionsResponse>

  /**
   * Retrieves the subscription sku for a given packageName
   * @param domain PackageName of the app from which we are requesting the sku
   * @param sku the product of the subscription
   */
  @GET("product/8.20200701/applications/{domain}/inapp/subscriptions/{sku}")
  fun getSkuSubscription(@Path("domain") domain: String,
                         @Path("sku") sku: String): Single<SubscriptionResponse>

  /**
   * Retrieves the token for a given subscription
   * @param domain PackageName of the app from which we are requesting the sku
   * @param sku the product of the subscription
   * @param currency The preferred currency to generate the in-app subscription token with, as an ISO 4217 alphabetic code.
   */
  @GET("product/8.20200701/applications/{domain}/inapp/subscriptions/{sku}/token")
  fun getSkuSubscriptionToken(@Path("domain") domain: String,
                              @Path("sku") sku: String,
                              @Query("currency") currency: String?): Single<String>

  /**
   * Retrieves subscription purchases of a given application
   * @param domain PackageName of the app from which we are requesting the sku
   * @param limit Limit of purchases to be returned per page (default 100, max 100)
   */
  @GET("product/8.20200701/applications/{domain}/inapp/subscription/purchases")
  fun getPurchases(@Path("domain") domain: String,
                   @Query("wallet.address") walletAddress: String,
                   @Query("wallet.signature") walletSignature: String,
                   @Query("limit") limit: Long? = null): Single<SubscriptionPurchaseListResponse>

  @GET("product/8.20200701/applications/{domain}/inapp/subscription/purchases/{uid}")
  fun getPurchase(@Path("domain") domain: String,
                  @Path("uid") uid: String, @Query("wallet.address") walletAddress: String,
                  @Query("wallet.signature")
                  walletSignature: String): Single<SubscriptionPurchaseResponse>

  @PATCH("product/8.20200701/applications/{domain}/inapp/subscription/purchases/{uid}")
  fun updatePurchase(@Path("domain") domain: String,
                     @Path("uid") uid: String,
                     @Query("wallet.address") walletAddress: String,
                     @Query("wallet.signature") walletSignature: String,
                     @Body purchaseUpdate: PurchaseUpdate): Completable

}