package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.*
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import java.math.BigDecimal

interface GamificationApi {  //
  @GET("gamification/1.20230531/user_stats")
  fun getUserStats(
    @Query("address") address: String,
    @Query("lang_code") languageCode: String,
    @Query("promo_code") promoCodeString: String?
  ): Single<UserStatusResponse>

  @GET("gamification/levels")
  fun getLevels(@Query("address") address: String): Single<LevelsResponse>

  @GET("gamification/bonus_forecast")
  fun getForecastBonus(
    @Query("address") wallet: String,
    @Query("package_name") packageName: String,
    @Query("amount") amount: BigDecimal,
    @Query("currency") currency: String,
    @Query("promo_code") promoCodeString: String?
  ): Single<ForecastBonusResponse>

  @GET("gamification/referral_info")
  fun getReferralInfo(): Single<ReferralResponse>

  @GET("gamification/perks/promo_code/")
  fun getVipReferral(
    @Query("wallet") wallet: String
  ): Single<VipReferralResponse>
}
