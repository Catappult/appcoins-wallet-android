package com.appcoins.wallet.gamification.repository

import com.appcoins.wallet.gamification.repository.entity.LevelsResponse
import com.appcoins.wallet.gamification.repository.entity.PromotionsResponse
import io.reactivex.Completable
import io.reactivex.Single

interface GamificationLocalData {
  /**
   * @return -1 if never showed any level
   */
  fun getLastShownLevel(wallet: String, screen: String): Single<Int>

  fun saveShownLevel(wallet: String, level: Int, screen: String): Completable

  fun setGamificationLevel(gamificationLevel: Int): Completable

  fun getSeenGenericPromotion(wallet: String, id: String, screen: String): Boolean

  fun setSeenGenericPromotion(wallet: String, id: String, screen: String): Completable

  fun getPromotions(): Single<List<PromotionsResponse>>

  fun deletePromotions(): Completable

  fun insertPromotions(promotions: List<PromotionsResponse>): Completable

  fun deleteLevels(): Completable

  fun getLevels(): Single<LevelsResponse>

  fun insertLevels(levels: LevelsResponse): Completable
}
