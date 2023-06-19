package com.appcoins.wallet.core.network.eskills.api

import com.appcoins.wallet.core.network.eskills.model.TopNPlayersResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface GeneralPlayerStats {
  @Headers("accept: application/json")
  @GET("room/statistics/v2/top_n_players")
  fun getTopNPlayers(
    @Query("package_name") packageName: String?,
    @Query("wallet_address") wallet_address: String?,
    @Query("time_frame") timeframe: String?,
    @Query("limit") limit: Int
  ): Single<TopNPlayersResponse>
}