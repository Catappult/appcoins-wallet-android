package com.appcoins.wallet.core.network.microservices.api.broker

import com.appcoins.wallet.core.network.microservices.model.*
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AdyenSessionApi {

  @POST("8.20200815/gateways/adyen_v2/session")
  fun createSessionTransaction(
    @Query("wallet.address") walletAddress: String,
    @Header("authorization") authorization: String,
    @Body sessionPaymentDetails: SessionPaymentDetails
  ): Single<AdyenSessionResponse>

}