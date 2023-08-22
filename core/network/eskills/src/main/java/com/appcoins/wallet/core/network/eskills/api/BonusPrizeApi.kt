package com.appcoins.wallet.core.network.eskills.api

import com.appcoins.wallet.core.network.eskills.model.BonusHistory
import com.appcoins.wallet.core.network.eskills.model.NextPrizeSchedule
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface BonusPrizeApi {
  @GET("room/bonus/bonus_history/list")
  fun getBonusHistoryList(
    @Query("package_name") package_name: String?,
    @Query("sku") sku: String?,
    @Query("time_frame") time_frame: String
  ): Single<List<BonusHistory>>

  @GET("room/bonus/next_schedule")
  fun getTimeUntilNextBonus(
    @Query("time_frame") time_frame: String
  ): Single<NextPrizeSchedule>
}