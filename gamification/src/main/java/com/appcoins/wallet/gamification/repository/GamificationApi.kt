package com.appcoins.wallet.gamification.repository

import com.appcoins.wallet.gamification.repository.entity.LevelsResponse
import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import com.appcoins.wallet.gamification.repository.entity.UserStatusResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import java.math.BigDecimal

interface GamificationApi {
  @GET("gamification/1.20210222/user_stats")
  fun getUserStats(@Query("address") address: String,
                   @Query("lang_code") languageCode: String): Single<UserStatusResponse>

  @GET("gamification/levels")
  fun getLevels(@Query("address") address: String): Single<LevelsResponse>

  @GET("gamification/bonus_forecast")
  fun getForecastBonus(@Query("address") wallet: String,
                       @Query("package_name") packageName: String,
                       @Query("amount") amount: BigDecimal, @Query("currency")
                       currency: String): Single<ForecastBonusResponse>

  @GET("gamification/referral_info")
  fun getReferralInfo(): Single<ReferralResponse>
}
