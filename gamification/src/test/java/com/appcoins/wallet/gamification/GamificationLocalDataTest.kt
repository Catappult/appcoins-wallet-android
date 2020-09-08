package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.GamificationLocalData
import com.appcoins.wallet.gamification.repository.entity.LevelsResponse
import com.appcoins.wallet.gamification.repository.entity.PromotionsResponse
import io.reactivex.Completable
import io.reactivex.Single

class GamificationLocalDataTest : GamificationLocalData {

  var lastShownLevelResponse: Single<Int>? = null
  var seenGenericPromotionResponse: Boolean? = null
  var userStatusResponse: Single<List<PromotionsResponse>>? = null
  var levelsResponse: Single<LevelsResponse>? = null
  private var wallet: String? = null
  private var gamificationLevel: Int? = -1

  override fun saveShownLevel(wallet: String, level: Int, screen: String): Completable {
    return Completable.fromAction {
      this.wallet = wallet
      lastShownLevelResponse = Single.just(level)
    }
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

  override fun deletePromotions(): Completable {
    return Completable.complete()
  }

  override fun getPromotions(): Single<List<PromotionsResponse>> {
    val aux = userStatusResponse!!
    userStatusResponse = null
    return aux
  }

  override fun insertPromotions(promotions: List<PromotionsResponse>): Completable {
    return Completable.complete()
  }

  override fun deleteLevels(): Completable {
    return Completable.complete()
  }

  override fun getLevels(): Single<LevelsResponse> {
    val aux = levelsResponse!!
    levelsResponse = null
    return aux
  }

  override fun insertLevels(levels: LevelsResponse): Completable {
    return Completable.complete()
  }
}