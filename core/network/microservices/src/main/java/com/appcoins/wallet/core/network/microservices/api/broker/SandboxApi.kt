package com.appcoins.wallet.core.network.microservices.api.broker

import com.appcoins.wallet.core.network.microservices.model.*
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface SandboxApi {

  @POST("8.20250730/gateways/sandbox/transactions")
  fun createTransaction(
    @Query("wallet.address") walletAddress: String,
    @Body sandboxPayment: SandboxPayment
  ): Single<SandboxResponse>

  @Headers("Content-Type: application/json;format=product_token")
  @POST("8.20250730/gateways/sandbox/transactions")
  fun createTokenTransaction(
    @Query("wallet.address") walletAddress: String,
    @Body sandboxPayment: SandboxTokenPayment
  ): Single<SandboxResponse>

}
