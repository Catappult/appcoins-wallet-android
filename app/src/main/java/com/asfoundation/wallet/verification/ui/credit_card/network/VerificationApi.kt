package com.asfoundation.wallet.verification.ui.credit_card.network

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface VerificationApi {

  @GET("transaction/verified_wallet")
  fun isUserVerified(@Query("wallet") wallet: String): Single<VerificationResponse>
}