package com.appcoins.wallet.core.network.microservices.api.product

import com.appcoins.wallet.core.network.microservices.model.*
import io.reactivex.Single
import retrofit2.http.*

interface TopUpValuesApi {
  @GET("8.20240308/topup/billing/domains/{packageName}")
  fun getInputLimitValues(
    @Path("packageName") packageName: String,
    @Query("currency") currency: String?,
    @Query("method") method: String?
  ): Single<TopUpLimitValuesResponse>

  @GET("8.20240308/topup/billing/domains/{packageName}/skus")
  fun getDefaultValues(
    @Path("packageName") packageName: String,
    @Query("currency") currency: String?
  ): Single<TopUpDefaultValuesResponse>
}