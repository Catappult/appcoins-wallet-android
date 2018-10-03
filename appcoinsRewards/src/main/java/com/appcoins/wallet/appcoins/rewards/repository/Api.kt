package com.appcoins.wallet.appcoins.rewards.repository

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {
  @GET("campaign/rewards")
  fun getBalance(@Query("address") address: String): Single<RewardBalanceResponse>

  data class RewardBalanceResponse(val balance: Long)
}
