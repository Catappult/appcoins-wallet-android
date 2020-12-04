package com.asfoundation.wallet.verification

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface VerificationApi {

  @GET("user_verified")
  fun isValid(@Query("address") wallet: String): Single<VerificationResponse>


}