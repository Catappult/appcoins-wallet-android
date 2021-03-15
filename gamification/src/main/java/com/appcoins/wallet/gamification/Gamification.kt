package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.*
import com.appcoins.wallet.gamification.repository.entity.GamificationResponse
import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal
import java.net.UnknownHostException

class Gamification(private val repository: PromotionsRepository) {

  companion object {
    const val GAMIFICATION_ID = "GAMIFICATION"
    const val REFERRAL_ID = "REFERRAL"
  }

  fun getUserStats(wallet: String): Observable<GamificationStats> {
    return repository.getGamificationStats(wallet)
  }

  fun getUserLevel(wallet: String): Single<Int> {
    return repository.getGamificationLevel(wallet)
  }

  fun getLevels(wallet: String, offlineFirst: Boolean = true): Observable<Levels> {
    return repository.getLevels(wallet, offlineFirst)
  }

  fun getUserBonusAndLevel(wallet: String): Single<ForecastBonusAndLevel> {
    return repository.getUserStats(wallet, false)
        .map { map(it) }
        .lastOrError()
        .onErrorReturn { mapReferralError(it) }
  }

  private fun map(userStats: UserStats): ForecastBonusAndLevel {
    val gamification = userStats.promotions
        .firstOrNull {
          it is GamificationResponse && it.id == GAMIFICATION_ID
        } as GamificationResponse?

    val referral = userStats.promotions
        .firstOrNull {
          it is GamificationResponse && it.id == REFERRAL_ID
        } as ReferralResponse?

    return if (referral == null || referral.pendingAmount.compareTo(BigDecimal.ZERO) == 0) {
      ForecastBonusAndLevel(status = ForecastBonus.Status.INACTIVE,
          level = gamification?.level ?: 0)
    } else {
      ForecastBonusAndLevel(
          ForecastBonus.Status.ACTIVE,
          referral.pendingAmount,
          minAmount = referral.minAmount,
          level = gamification?.level ?: 0)
    }
  }

  private fun mapReferralError(throwable: Throwable): ForecastBonusAndLevel {
    throwable.printStackTrace()
    return when (throwable) {
      is UnknownHostException -> ForecastBonusAndLevel(ForecastBonus.Status.NO_NETWORK)
      else -> {
        ForecastBonusAndLevel(ForecastBonus.Status.UNKNOWN_ERROR)
      }
    }
  }

  fun getEarningBonus(wallet: String, packageName: String,
                      amount: BigDecimal): Single<ForecastBonus> {
    return repository.getForecastBonus(wallet, packageName, amount)
  }

  fun hasNewLevel(wallet: String, gamificationContext: GamificationContext,
                  level: Int): Single<Boolean> {
    return repository.getLastShownLevel(wallet, gamificationContext)
        .map { lastShownLevel: Int -> lastShownLevel < level }
  }

  fun levelShown(wallet: String, level: Int, gamificationContext: GamificationContext):
      Completable {
    return Completable.fromAction { repository.shownLevel(wallet, level, gamificationContext) }
  }

}
