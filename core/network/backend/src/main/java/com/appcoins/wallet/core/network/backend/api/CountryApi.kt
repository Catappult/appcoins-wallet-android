package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.CountryResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header

interface CountryApi {
  @GET("appc/countrycode")
  fun getCountryCode(@Header("x-client-ip") clientIp: String?): Single<CountryResponse>
}