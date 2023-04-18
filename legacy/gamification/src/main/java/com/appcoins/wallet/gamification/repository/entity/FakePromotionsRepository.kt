package com.appcoins.wallet.gamification.repository.entity

import com.appcoins.wallet.core.network.backend.model.ReferralResponse
import com.appcoins.wallet.core.network.backend.model.VipReferralResponse
import com.appcoins.wallet.core.network.backend.model.WalletOrigin
import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.*
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal

class FakePromotionsRepository : PromotionsRepository{

  private val levelsMap = mutableMapOf<String, Int>()

  override fun getGamificationStats(
    wallet: String,
    promoCodeString: String?
  ): Observable<PromotionsGamificationStats> {
    TODO("Not yet implemented")
  }

  override fun getGamificationLevel(wallet: String, promoCodeString: String?): Single<Int> {
    TODO("Not yet implemented")
  }

  override fun getLevels(wallet: String, offlineFirst: Boolean): Observable<Levels> {
    TODO("Not yet implemented")
  }

  override fun getForecastBonus(
    wallet: String,
    packageName: String,
    amount: BigDecimal,
    promoCodeString: String?
  ): Single<ForecastBonus> {
    TODO("Not yet implemented")
  }

  override fun getLastShownLevel(
    wallet: String,
    gamificationContext: GamificationContext
  ): Single<Int> {
    return Single.fromCallable{
      levelsMap[wallet + gamificationContext] ?: PromotionsGamificationStats.INVALID_LEVEL
    }
  }

  override fun shownLevel(wallet: String, level: Int, gamificationContext: GamificationContext) {
    levelsMap[wallet + gamificationContext] = level
  }

  override fun getSeenGenericPromotion(id: String, screen: String): Boolean {
    TODO("Not yet implemented")
  }

  override fun setSeenGenericPromotion(id: String, screen: String) {
    TODO("Not yet implemented")
  }

  override fun getUserStats(
    wallet: String,
    promoCodeString: String?,
    offlineFirst: Boolean
  ): Observable<UserStats> {
    TODO("Not yet implemented")
  }

  override fun getWalletOrigin(wallet: String, promoCodeString: String?): Single<WalletOrigin> {
    TODO("Not yet implemented")
  }

  override fun getReferralUserStatus(
    wallet: String,
    promoCodeString: String?
  ): Single<ReferralResponse> {
    TODO("Not yet implemented")
  }

  override fun getReferralInfo(): Single<ReferralResponse> {
    TODO("Not yet implemented")
  }

  override fun getVipReferral(wallet: String): Single<VipReferralResponse> {
    TODO("Not yet implemented")
  }

  override fun isVipCalloutAlreadySeen(wallet: String): Boolean {
    TODO("Not yet implemented")
  }

  override fun setVipCalloutAlreadySeen(wallet: String, isSeen: Boolean) {
    TODO("Not yet implemented")
  }

  override fun isReferralNotificationToShow(wallet: String): Observable<Boolean> {
    TODO("Not yet implemented")
  }

  override fun setReferralNotificationSeen(wallet: String, isSeen: Boolean) {
    TODO("Not yet implemented")
  }

}