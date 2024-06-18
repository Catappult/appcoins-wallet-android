package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.CountryResponse
import io.reactivex.Single
import retrofit2.http.GET

interface CountryApi {
  @GET("appc/countrycode")
  fun getCountryCode(): Single<CountryResponse?>?

  @GET("appc/countrycode")
  fun getCountryCodeForRefund(): Single<CountryResponse>
}