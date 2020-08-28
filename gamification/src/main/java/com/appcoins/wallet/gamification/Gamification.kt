package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.*
import com.appcoins.wallet.gamification.repository.entity.GamificationResponse
import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import com.appcoins.wallet.gamification.repository.entity.UserStatusResponse
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.net.UnknownHostException

class Gamification(private val repository: PromotionsRepository) {

  companion object {
    const val GAMIFICATION_ID = "GAMIFICATION"
    const val REFERRAL_ID = "REFERRAL"
    const val PROGRESS_VIEW_TYPE = "PROGRESS"
  }

  fun getUserStats(wallet: String): Single<GamificationStats> {
    return repository.getGamificationStats(wallet)
  }

  fun getLevels(wallet: String): Single<Levels> {
    return repository.getLevels(wallet)
  }

  fun getUserBonusAndLevel(wallet: String): Single<ForecastBonusAndLevel> {
    return repository.getUserStatus(wallet)
        .map { map(it) }
        .onErrorReturn { mapReferralError(it) }
  }

  private fun map(userStats: UserStatusResponse): ForecastBonusAndLevel {
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

  fun hasNewLevel(wallet: String, screen: String): Single<Boolean> {
    return Single.zip(repository.getLastShownLevel(wallet, screen), getUserStats(wallet),
        BiFunction { lastShownLevel: Int, gamificationStats: GamificationStats ->
          gamificationStats.status == GamificationStats.Status.OK && lastShownLevel < gamificationStats.level
        })
  }

  fun levelShown(wallet: String, level: Int, screen: String): Completable {
    return repository.shownLevel(wallet, level, screen)
  }

}
