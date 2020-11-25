package com.asfoundation.wallet.verification

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface WalletVerificationApi {

  @GET("user_verified")
  fun isValid(@Query("address") wallet: String): Single<WalletVerificationResponse>


}