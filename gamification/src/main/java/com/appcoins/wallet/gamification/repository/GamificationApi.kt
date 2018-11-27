package com.appcoins.wallet.gamification.repository

import com.appcoins.wallet.gamification.repository.entity.LevelsResponse
import com.appcoins.wallet.gamification.repository.entity.UserStatusResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface GamificationApi {
  @GET("user_stats")
  fun getUserStatus(@Query("address") address: String): Single<UserStatusResponse>

  @GET("gamification/levels")
  fun getLevels(): Single<LevelsResponse>
}
