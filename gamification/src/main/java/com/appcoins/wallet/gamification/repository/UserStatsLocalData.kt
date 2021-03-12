package com.appcoins.wallet.gamification.repository

import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.entity.LevelsResponse
import com.appcoins.wallet.gamification.repository.entity.PromotionsResponse
import com.appcoins.wallet.gamification.repository.entity.WalletOrigin
import io.reactivex.Completable
import io.reactivex.Single

interface UserStatsLocalData {
  /**
   * @return -1 if never showed any level
   */
  fun getLastShownLevel(wallet: String, gamificationContext: GamificationContext): Single<Int>

  fun saveShownLevel(wallet: String, level: Int, gamificationContext: GamificationContext)

  fun setGamificationLevel(gamificationLevel: Int): Completable

  fun getGamificationLevel(): Int

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

  fun shouldShowGamificationDisclaimer(): Boolean

  fun setGamificationDisclaimerShown()

  fun setSeenWalletOrigin(wallet: String, walletOrigin: String)

  fun getSeenWalletOrigin(wallet: String): String
}
