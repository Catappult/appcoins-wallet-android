package com.appcoins.wallet.core.network.microservices.model

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface UserSubscriptionApi {

  /**
   * Retrieves all subscriptions for a given user
   * @param subStatus Filter based on the subStatus of the subscription
   * @param applicationName Filter based on the package name
   * @param limit Limit for the max number of subscriptions to be returned
   */
  @GET("8.20200701/application/inapp/subscription/purchases")
  fun getUserSubscriptions(@Header("Accept-Language") language: String,
                           @Query("wallet.address") walletAddress: String,
                           @Query("wallet.signature") walletSignature: String,
                           @Header("authorization") authorization: String,
                           @Query("substatus") subStatus: String? = null,
                           @Query("limit") limit: Int? = null,
                           @Query("application.name")
                           applicationName: String? = null): Single<UserSubscriptionsListResponse>
}