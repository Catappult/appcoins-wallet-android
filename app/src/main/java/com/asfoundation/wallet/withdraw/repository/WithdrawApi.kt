package com.asfoundation.wallet.withdraw.repository

import io.reactivex.Completable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface WithdrawApi {
  @POST("transaction/wallet/withdraw/credits")
  fun withdrawAppcCredits(
    @Header("authorization") authorization: String,
    @Body withdrawBody: WithdrawBody
  ): Completable
}