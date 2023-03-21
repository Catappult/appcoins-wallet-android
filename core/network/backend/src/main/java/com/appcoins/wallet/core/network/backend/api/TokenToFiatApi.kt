package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.AppcToFiatResponseBody
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface TokenToFiatApi {
  @GET("appc/value")
  fun getAppcToFiatRate(
    @Query("currency") currency: String?
  ): Observable<AppcToFiatResponseBody?>?
}