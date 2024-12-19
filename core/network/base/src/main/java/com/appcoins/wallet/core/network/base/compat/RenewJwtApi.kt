package com.appcoins.wallet.core.network.base.compat

import com.appcoins.wallet.core.network.base.model.JwtResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header

interface RenewJwtApi {

  @GET("appc/token/jwt")
  fun renewJwt(@Header("Authorization") ewt: String): Single<JwtResponse>
}