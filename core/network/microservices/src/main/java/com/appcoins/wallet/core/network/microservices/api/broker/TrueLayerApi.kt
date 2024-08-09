package com.appcoins.wallet.core.network.microservices.api.broker

import com.appcoins.wallet.core.network.microservices.model.*
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface TrueLayerApi {

  @POST("8.20240627/gateways/truelayer/transactions")
  fun createTransaction(
    @Query("wallet.address") walletAddress: String,
    @Header("authorization") authorization: String,
    @Body trueLayerPayment: TrueLayerPayment
  ): Single<TrueLayerResponse>

}
