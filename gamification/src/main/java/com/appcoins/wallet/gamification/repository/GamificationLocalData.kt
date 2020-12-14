package com.appcoins.wallet.gamification.repository

import com.appcoins.wallet.gamification.repository.entity.LevelsResponse
import com.appcoins.wallet.gamification.repository.entity.PromotionsResponse
import com.appcoins.wallet.gamification.repository.entity.WalletOrigin
import io.reactivex.Completable
import io.reactivex.Single

interface GamificationLocalData {
  /**
   * @return -1 if never showed any level
   */
  fun getLastShownLevel(wallet: String, screen: String): Single<Int>

  fun saveShownLevel(wallet: String, level: Int, screen: String)

  fun setGamificationLevel(gamificationLevel: Int): Completable

  fun getSeenGenericPromotion(id: String, screen: String): Boolean

  fun setSeenGenericPromotion(id: String, screen: String)

  fun getPromotions(): Single<List<PromotionsResponse>>

  fun deletePromotions(): Completable

  fun insertPromotions(promotions: List<PromotionsResponse>): Completable

  fun deleteLevels(): Completable

  fun getLevels(): Single<LevelsResponse>

  fun insertLevels(levels: LevelsResponse): Completable

  fun insertWalletOrigin(wallet: String, walletOrigin: WalletOrigin): Completable

  fun retrieveWalletOrigin(wallet: String): Single<WalletOrigin>
}
