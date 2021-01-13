package com.asfoundation.wallet.verification.network

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ValidationApi {

  @GET("verification/state")
  fun getValidationState(@Query("wallet.address") wallet: String,
                         @Query("wallet.signature") walletSignature: String): Single<String>
}