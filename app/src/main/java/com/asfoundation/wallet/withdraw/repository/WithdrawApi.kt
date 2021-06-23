package com.asfoundation.wallet.withdraw.repository

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface WithdrawApi {
  @POST("transaction/wallet/withdraw/credits")
  fun withdrawAppcCredits(@Body withdrawBody: WithdrawBody): Completable
}