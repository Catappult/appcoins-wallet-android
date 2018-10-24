package com.appcoins.wallet.appcoins.rewards.repository.backend

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import java.math.BigDecimal

interface BackendApi {
  @GET("campaign/rewards")
  fun getBalance(@Query("address") address: String): Single<RewardBalanceResponse>

  data class RewardBalanceResponse(val balance: BigDecimal)
}
