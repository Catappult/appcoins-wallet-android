package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.ForecastBonusResponse
import com.appcoins.wallet.gamification.repository.GamificationApi
import com.appcoins.wallet.gamification.repository.entity.LevelsResponse
import com.appcoins.wallet.gamification.repository.entity.UserStatusResponse
import io.reactivex.Single
import java.math.BigDecimal

class GamificationApiTest : GamificationApi {
  var userStatusResponse: Single<UserStatusResponse>? = null
  var levelsResponse: Single<LevelsResponse>? = null
  var bonusResponse: Single<ForecastBonusResponse>? = null
  override fun getUserStatus(address: String): Single<UserStatusResponse> {
    val aux = userStatusResponse!!
    userStatusResponse = null
    return aux
  }

  override fun getLevels(): Single<LevelsResponse> {
    val aux = levelsResponse!!
    levelsResponse = null
    return aux
  }

  override fun getForecastBonus(wallet: String, packageName: String, amount: BigDecimal,
                                currency: String): Single<ForecastBonusResponse> {
    val aux = bonusResponse!!
    bonusResponse = null
    return aux
  }
}
