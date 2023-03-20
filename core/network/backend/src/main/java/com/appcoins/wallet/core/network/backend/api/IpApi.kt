package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.IpResponse
import io.reactivex.Single
import retrofit2.http.GET

interface IpApi {
  @GET("appc/countrycode")
  fun myIp(): Single<IpResponse?>?
}