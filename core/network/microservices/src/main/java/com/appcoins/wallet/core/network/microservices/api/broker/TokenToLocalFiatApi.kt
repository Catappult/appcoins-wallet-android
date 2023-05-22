package com.appcoins.wallet.core.network.microservices.api.broker

import com.appcoins.wallet.core.network.microservices.model.ConversionResponseBody
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TokenToLocalFiatApi {
  @GET("8.20180518/exchanges/{currency}/convert/{value}")
  fun getValueToTargetFiat(
    @Path("currency") currency: String,
    @Path("value") value: String
  ): Single<ConversionResponseBody>

  @GET("8.20180518/exchanges/{currency}/convert/{value}")
  fun getValueToTargetFiat(
    @Path("currency") currency: String,
    @Path("value") value: String,
    @Query("to")
    targetCurrency: String
  ): Single<ConversionResponseBody>

  @GET("8.20180518/exchanges/{currency}/convert/{value}?to=APPC")
  fun convertFiatToAppc(
    @Path("currency") currency: String,
    @Path("value") value: String
  ): Single<ConversionResponseBody>
}