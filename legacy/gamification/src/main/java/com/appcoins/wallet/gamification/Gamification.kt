package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.ForecastBonus
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.PromotionsGamificationStats
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal
import javax.inject.Inject

class Gamification @Inject constructor(private val repository: PromotionsRepository) {

  companion object {
    const val GAMIFICATION_ID = "GAMIFICATION"
    const val REFERRAL_ID = "REFERRAL"
  }

  fun getUserStats(
    wallet: String,
    promoCodeString: String?,
    offlineFirst: Boolean = false
  ): Observable<PromotionsGamificationStats> =
    repository.getGamificationStats(
      wallet = wallet,
      promoCodeString = promoCodeString,
      offlineFirst = offlineFirst
    )

  fun getUserLevel(wallet: String, promoCodeString: String?): Single<Int> =
    repository.getGamificationLevel(
      wallet = wallet,
      promoCodeString = promoCodeString
    )

  fun getLevels(wallet: String, offlineFirst: Boolean = true): Observable<Levels> =
    repository.getLevels(
      wallet = wallet,
      offlineFirst = offlineFirst
    )

  fun getEarningBonus(
    wallet: String,
    packageName: String,
    amount: BigDecimal,
    promoCodeString: String?,
    currency: String?
  ): Single<ForecastBonus> =
    repository.getForecastBonus(
      wallet = wallet,
      packageName = packageName,
      amount = amount,
      promoCodeString = promoCodeString,
      currency = currency
    )

  fun hasNewLevel(
    wallet: String, gamificationContext: GamificationContext,
    level: Int
  ): Single<Boolean> =
    repository.getLastShownLevel(
      wallet = wallet,
      gamificationContext = gamificationContext
    ).map { it < level }

  fun levelShown(
    wallet: String,
    level: Int,
    gamificationContext: GamificationContext
  ): Completable =
    Completable.fromAction {
      repository.shownLevel(
        wallet = wallet,
        level = level,
        gamificationContext = gamificationContext
      )
    }

}
