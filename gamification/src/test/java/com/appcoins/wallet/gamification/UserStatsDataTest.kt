package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.UserStatsLocalData
import com.appcoins.wallet.gamification.repository.entity.LevelsResponse
import com.appcoins.wallet.gamification.repository.entity.PromotionsResponse
import com.appcoins.wallet.gamification.repository.entity.WalletOrigin
import io.reactivex.Completable
import io.reactivex.Single

class UserStatsDataTest : UserStatsLocalData {

  var lastShownLevelResponse: Single<Int>? = null
  private var seenGenericPromotionResponse: Boolean? = null
  var userStatusResponse: Single<List<PromotionsResponse>>? = null
  var walletOriginResponse: Single<WalletOrigin>? = null
  var levelsResponse: Single<LevelsResponse>? = null
  private var wallet: String? = null
  private var gamificationLevel: Int? = -1

  override fun saveShownLevel(wallet: String, level: Int, screen: String) {
    this.wallet = wallet
    lastShownLevelResponse = Single.just(level)
  }

  fun getWallet(): String {
    val aux = wallet!!
    wallet = null
    return aux
  }

  override fun getLastShownLevel(wallet: String, screen: String): Single<Int> {
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

  override fun setGamificationLevel(gamificationLevel: Int): Completable {
    return Completable.fromAction {
      this.gamificationLevel = gamificationLevel
    }
  }

  override fun getGamificationLevel(): Int = -1

  override fun deletePromotions(): Completable = Completable.complete()

  override fun getPromotions(): Single<List<PromotionsResponse>> {
    val aux = userStatusResponse!!
    userStatusResponse = null
    return aux
  }

  override fun insertPromotions(promotions: List<PromotionsResponse>): Completable {
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
}