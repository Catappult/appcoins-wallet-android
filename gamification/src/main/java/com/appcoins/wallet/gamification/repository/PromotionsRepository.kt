package com.appcoins.wallet.gamification.repository

import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import com.appcoins.wallet.gamification.repository.entity.VipReferralResponse
import com.appcoins.wallet.gamification.repository.entity.WalletOrigin
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal

interface PromotionsRepository {

  fun getGamificationStats(wallet: String, promoCodeString: String?): Observable<PromotionsGamificationStats>

  fun getGamificationLevel(wallet: String, promoCodeString: String?): Single<Int>

  fun getLevels(wallet: String, offlineFirst: Boolean = true): Observable<Levels>

  fun getForecastBonus(wallet: String, packageName: String,
                       amount: BigDecimal, promoCodeString: String?): Single<ForecastBonus>

  fun getLastShownLevel(wallet: String, gamificationContext: GamificationContext): Single<Int>

  fun shownLevel(wallet: String, level: Int, gamificationContext: GamificationContext)

  fun getSeenGenericPromotion(id: String, screen: String): Boolean

  fun setSeenGenericPromotion(id: String, screen: String)

  fun getUserStats(wallet: String, promoCodeString: String?,
                   offlineFirst: Boolean = true): Observable<UserStats>

  fun getWalletOrigin(wallet: String, promoCodeString: String?): Single<WalletOrigin>

  fun getReferralUserStatus(wallet: String, promoCodeString: String?): Single<ReferralResponse>

  fun getReferralInfo(): Single<ReferralResponse>

  fun getVipReferral(wallet: String): Observable<VipReferralResponse>

  fun isVipCalloutAlreadySeen(): Boolean

  fun setVipCalloutAlreadySeen(isSeen: Boolean)

}
