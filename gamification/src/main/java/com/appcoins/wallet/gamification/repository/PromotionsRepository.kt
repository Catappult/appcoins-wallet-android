package com.appcoins.wallet.gamification.repository

import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import com.appcoins.wallet.gamification.repository.entity.UserStatusResponse
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal

interface PromotionsRepository {

  fun getGamificationStats(wallet: String): Observable<GamificationStats>

  fun getLevels(wallet: String): Single<Levels>

  fun getForecastBonus(wallet: String, packageName: String,
                       amount: BigDecimal): Single<ForecastBonus>

  fun getLastShownLevel(wallet: String, gamificationContext: GamificationContext): Single<Int>

  fun shownLevel(wallet: String, level: Int, gamificationContext: GamificationContext)

  fun getSeenGenericPromotion(id: String, screen: String): Boolean

  fun setSeenGenericPromotion(id: String, screen: String)

  fun getUserStatus(wallet: String): Single<UserStatusResponse>

  fun getUserStatusDbFirst(wallet: String): Observable<UserStatusResponse>

  fun getReferralUserStatus(wallet: String): Single<ReferralResponse>

  fun getReferralInfo(): Single<ReferralResponse>
}
