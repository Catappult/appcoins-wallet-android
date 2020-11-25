package com.appcoins.wallet.bdsbilling

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface SubscriptionBillingApi {

  @GET("{domain}/inapp")
  fun getPackage(@Path("domain") packageName: String): Single<Boolean>

  /**
   * Retrieves all subscriptions for a given packageName
   * @param domain PackageName of the app which we are requesting the sku
   * @param skus (optionnal) list of SKUs to filter by. up to 100, which may be set as a comma separated list of values, or as a JSON or query string array.
   * @param limit (optionnal) The limit on the maximum number of items to return per page, up to 100.
   * @param currency (optionnal) The preferred currency to return prices in, as an ISO 4217 alphabetic code.
   */
  @GET("{domain}/inapp/subscriptions")
  fun getSubscriptions(@Header("Accept-Language") language: String,
                       @Path("domain") domain: String,
                       @Query("skus") skus: List<String>?,
                       @Query("limit") limit: Long? = null,
                       @Query("currency") currency: String? = null): Single<SubscriptionsResponse>

  /**
   * Retrieves the subscription sku for a given packageName
   * @param domain PackageName of the app from which we are requesting the sku
   * @param sku the product of the subscription
   */
  @GET("{domain}/inapp/subscriptions/{sku}")
  fun getSkuSubscription(@Header("Accept-Language") language: String,
                         @Path("domain") domain: String,
                         @Path("sku") sku: String): Single<SubscriptionResponse>

  /**
   * Retrieves the token for a given subscription
   * @param domain PackageName of the app from which we are requesting the sku
   * @param sku the product of the subscription
   * @param currency The preferred currency to generate the in-app subscription token with, as an ISO 4217 alphabetic code.
   */
  @GET("{domain}/inapp/subscriptions/{sku}/token")
  fun getSkuSubscriptionToken(@Path("domain") domain: String,
                              @Path("sku") sku: String,
                              @Query("currency") currency: String?,
                              @Query("wallet.address") walletAddress: String,
                              @Query("wallet.signature") walletSignature: String): Single<String>

  /**
   * Retrieves subscription purchases of a given application
   * @param domain PackageName of the app from which we are requesting the sku
   * @param limit (optional) Limit of purchases to be returned per page (default 100, max 100)
   */
  @GET("{domain}/inapp/subscription/purchases")
  fun getPurchases(@Path("domain") domain: String,
                   @Query("wallet.address") walletAddress: String,
                   @Query("wallet.signature") walletSignature: String,
                   @Query("limit") limit: Long? = null): Single<SubscriptionPurchaseListResponse>

  @GET("{domain}/inapp/subscription/purchases/{uid}")
  fun getPurchase(@Path("domain") domain: String,
                  @Path("uid") uid: String,
                  @Query("wallet.address") walletAddress: String,
                  @Query("wallet.signature")
                  walletSignature: String): Single<SubscriptionPurchaseResponse>

  @POST("{domain}/inapp/subscription/purchases/{uid}/consume")
  fun consumePurchase(@Path("domain") domain: String,
                      @Path("uid") uid: String,
                      @Query("wallet.address") walletAddress: String,
                      @Query("wallet.signature") walletSignature: String,
                      @Query("payload") payload: String? = null): Completable

  @POST("{domain}/inapp/subscription/purchases/{uid}/acknowledge")
  fun acknowledgePurchase(@Path("domain") domain: String,
                          @Path("uid") uid: String,
                          @Query("wallet.address") walletAddress: String,
                          @Query("wallet.signature") walletSignature: String,
                          @Query("payload") payload: String? = null): Completable

  @POST("{domain}/inapp/subscription/purchases/{uid}/activate")
  fun activateSubscription(@Path("domain") domain: String,
                           @Path("uid") uid: String,
                           @Query("wallet.address") walletAddress: String,
                           @Query("wallet.signature") walletSignature: String): Completable

  @POST("{domain}/inapp/subscription/purchases/{uid}/cancel")
  fun cancelSubscription(@Path("domain") domain: String,
                         @Path("uid") uid: String,
                         @Query("wallet.address") walletAddress: String,
                         @Query("wallet.signature") walletSignature: String): Completable
}