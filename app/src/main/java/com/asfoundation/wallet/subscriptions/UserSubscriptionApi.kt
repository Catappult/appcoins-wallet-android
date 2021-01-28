package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.bdsbilling.subscriptions.UserSubscriptionsListResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface UserSubscriptionApi {

  /**
   * Retrieves all subscriptions for a given user
   * @param subStatus Filter based on the subStatus of the subscription
   * @param applicationName Filter based on the package name
   * @param limit Limit for the max number of subscriptions to be returned
   */
  @GET("inapp/subscription/purchases")
  @Headers("Cache-Control: public, max-stale=120")
  fun getUserSubscriptions(@Query("wallet.address") walletAddress: String,
                           @Query("wallet.signature") walletSignature: String,
                           @Query("substatus") subStatus: String?,
                           @Query("application.name") applicationName: String?,
                           @Query("limit") limit: Int?): Single<UserSubscriptionsListResponse>
}