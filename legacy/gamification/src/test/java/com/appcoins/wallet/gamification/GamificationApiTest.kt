package com.appcoins.wallet.gamification

import com.appcoins.wallet.core.network.backend.model.ForecastBonusResponse
import com.appcoins.wallet.core.network.backend.api.GamificationApi
import com.appcoins.wallet.core.network.backend.model.LevelsResponse
import com.appcoins.wallet.core.network.backend.model.ReferralResponse
import com.appcoins.wallet.core.network.backend.model.UserStatusResponse
import com.appcoins.wallet.core.network.backend.model.VipReferralResponse
import io.reactivex.Single
import java.math.BigDecimal

class GamificationApiTest : GamificationApi {
  var userStatusResponse: Single<UserStatusResponse>? = null
  var levelsResponse: Single<LevelsResponse>? = null
  var bonusResponse: Single<ForecastBonusResponse>? = null
  private var referralResponse: Single<ReferralResponse>? = null
  var vipReferralResponse: Single<VipReferralResponse>? = null

  override fun getUserStats(address: String, languageCode: String, promoCodeString: String?): Single<UserStatusResponse> {
    val aux = userStatusResponse!!
    userStatusResponse = null
    return aux
  }

  override fun getLevels(address: String): Single<LevelsResponse> {
    val aux = levelsResponse!!
    levelsResponse = null
    return aux
  }

  override fun getForecastBonus(wallet: String, packageName: String, amount: BigDecimal,
                                currency: String, promoCodeString: String?): Single<ForecastBonusResponse> {
    val aux = bonusResponse!!
    bonusResponse = null
    return aux
  }

  override fun getReferralInfo(): Single<ReferralResponse> {
    val aux = referralResponse!!
    referralResponse = null
    return aux
  }

  override fun getVipReferral(wallet: String): Single<VipReferralResponse> {
    val aux = vipReferralResponse!!
    vipReferralResponse = null
    return aux
  }
}
