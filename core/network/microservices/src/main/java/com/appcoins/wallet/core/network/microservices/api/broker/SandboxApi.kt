package com.appcoins.wallet.core.network.microservices.api.broker

import com.appcoins.wallet.core.network.microservices.model.*
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface SandboxApi {

  @POST("8.20240524/gateways/sandbox/transactions")
  fun createTransaction(
    @Query("wallet.address") walletAddress: String,
    @Body sandboxPayment: SandboxPayment
  ): Single<SandboxResponse>

}
