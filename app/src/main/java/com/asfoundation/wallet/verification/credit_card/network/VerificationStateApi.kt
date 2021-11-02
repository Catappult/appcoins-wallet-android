package com.asfoundation.wallet.verification.credit_card.network

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface VerificationStateApi {

  @GET("verification/state")
  fun getVerificationState(@Query("wallet.address") wallet: String,
                           @Query("wallet.signature") walletSignature: String): Single<String>
}