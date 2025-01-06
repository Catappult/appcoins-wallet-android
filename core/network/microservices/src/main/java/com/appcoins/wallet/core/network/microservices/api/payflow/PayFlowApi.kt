package com.appcoins.wallet.core.network.microservices.api.payflow

import com.appcoins.wallet.core.network.microservices.model.PayFlowResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface PayFlowApi {

  @GET("v2/payment_flow")
  fun getPayFlow(
    @Query("package") packageName: String,
    @Query("package_vercode") packageVercode: Int?,
    @Query("oemid") oemid: String?,
  ): Single<PayFlowResponse>

}
