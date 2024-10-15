package com.appcoins.wallet.core.network.backend.api

import io.reactivex.Completable
import retrofit2.http.GET

interface ImpressionApi {
  @GET("appc/games/impression")
  fun getImpression(): Completable
}