package com.appcoins.wallet.gamification.repository

import com.appcoins.wallet.gamification.repository.entity.LevelsResponse
import com.appcoins.wallet.gamification.repository.entity.UserStatusResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import java.math.BigDecimal

interface GamificationApi {
  @GET("gamification/user_stats")
  fun getUserStatus(@Query("address") address: String): Single<UserStatusResponse>

  @GET("gamification/levels")
  fun getLevels(): Single<LevelsResponse>

  @GET("gamification/bonus_forecast")
  fun getForecastBonus(@Query("address") wallet: String, @Query("package_name") packageName: String,
                       @Query("amount") amount: BigDecimal, @Query("currency")
                       currency: String): Single<ForecastBonusResponse>
}
