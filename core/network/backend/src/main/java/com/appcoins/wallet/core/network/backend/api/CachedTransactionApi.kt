package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.CachedTransactionResponse
import io.reactivex.Single
import retrofit2.http.GET

interface CachedTransactionApi {
  @GET("/transaction/inapp/cached_values")
  fun getCachedTransaction(): Single<CachedTransactionResponse>
}