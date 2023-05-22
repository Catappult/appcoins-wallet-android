package com.appcoins.wallet.core.network.microservices.api.product

import com.appcoins.wallet.core.network.microservices.model.*
import io.reactivex.Single
import retrofit2.http.*

interface TopUpValuesApi {
  @GET("8.20180518/topup/billing/domains/{packageName}")
  fun getInputLimitValues(
    @Path("packageName")
    packageName: String
  ): Single<TopUpLimitValuesResponse>

  @GET("8.20200402/topup/billing/domains/{packageName}/skus")
  fun getDefaultValues(
    @Path("packageName") packageName: String
  ): Single<TopUpDefaultValuesResponse>
}