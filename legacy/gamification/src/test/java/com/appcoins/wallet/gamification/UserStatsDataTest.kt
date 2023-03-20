package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.PromotionsGamificationStats
import com.appcoins.wallet.gamification.repository.UserStatsLocalData
import com.appcoins.wallet.core.network.backend.model.LevelsResponse
import com.appcoins.wallet.core.network.backend.model.PromotionsResponse
import com.appcoins.wallet.core.network.backend.model.WalletOrigin
import io.reactivex.Completable
import io.reactivex.Single

class UserStatsDataTest : UserStatsLocalData {

  var lastShownLevelResponse: Single<Int>? = null
  private var seenGenericPromotionResponse: Boolean? = null
  var userStatusResponse: Single<List<PromotionsResponse>>? = null
  var walletOriginResponse: Single<WalletOrigin>? = null
  var levelsResponse: Single<LevelsResponse>? = null
  private var wallet: String? = null
  private var gamificationLevel: Int? = PromotionsGamificationStats.INVALID_LEVEL

  override fun saveShownLevel(
    wallet: String, level: Int,
    gamificationContext: GamificationContext
  ) {
    this.wallet = wallet
    lastShownLevelResponse = Single.just(level)
  }

  fun getWallet(): String {
    val aux = wallet!!
    wallet = null
    return aux
  }

  override fun getLastShownLevel(wallet: String, gamificationContext: GamificationContext):
      Single<Int> {
    val aux = lastShownLevelResponse!!
    lastShownLevelResponse = null
    return aux
  }

  override fun getSeenGenericPromotion(id: String, screen: String): Boolean {
    val aux = seenGenericPromotionResponse!!
    seenGenericPromotionResponse = null
    return aux
  }

  override fun setSeenGenericPromotion(id: String, screen: String) {
    seenGenericPromotionResponse = true
  }

  override fun setGamificationLevel(gamificationLevel: Int) {
    this.gamificationLevel = gamificationLevel
  }

  override fun getGamificationLevel(): Int = PromotionsGamificationStats.INVALID_LEVEL


  override fun getPromotions(): Single<List<PromotionsResponse>> {
    val aux = userStatusResponse!!
    userStatusResponse = null
    return aux
  }

  override fun deleteAndInsertPromotions(promotions: List<PromotionsResponse>): Completable {
    return Completable.complete()
  }

  override fun deleteLevels(): Completable = Completable.complete()

  override fun getLevels(): Single<LevelsResponse> {
    val aux = levelsResponse!!
    levelsResponse = null
    return aux
  }

  override fun insertLevels(levels: LevelsResponse): Completable = Completable.complete()

  override fun insertWalletOrigin(wallet: String, walletOrigin: WalletOrigin): Completable {
    return Completable.complete()
  }

  override fun retrieveWalletOrigin(wallet: String): Single<WalletOrigin> {
    val aux = walletOriginResponse!!
    walletOriginResponse = null
    return aux
  }

  override fun shouldShowGamificationDisclaimer(): Boolean = true

  override fun setGamificationDisclaimerShown() = Unit

  override fun setSeenWalletOrigin(wallet: String, walletOrigin: String) = Unit

  override fun getSeenWalletOrigin(wallet: String): String = "APTOIDE"

  override fun isVipCalloutAlreadySeen(wallet: String): Boolean = false

  override fun setVipCalloutAlreadySeen(wallet: String, isSeen: Boolean) = Unit

  override fun isReferralNotificationSeen(wallet: String): Boolean = false

  override fun setReferralNotificationSeen(wallet: String, isSeen: Boolean) = Unit

}