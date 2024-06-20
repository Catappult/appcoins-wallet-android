package com.appcoins.wallet.core.network.microservices.api.broker

import com.appcoins.wallet.core.network.microservices.model.*
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface TrueLayerApi {

  @POST("8.20230522/gateways/truelayer/transactions")  //TODO check gateway and version
  fun createTransaction(
    @Query("wallet.address") walletAddress: String,
    @Header("authorization") authorization: String,
    @Body trueLayerPayment: TrueLayerPayment  //TODO
  ): Single<TrueLayerResponse>  //TODO

}
