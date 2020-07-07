package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.*
import com.appcoins.wallet.gamification.repository.entity.UserStatusResponse
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.net.UnknownHostException

class Gamification(private val repository: PromotionsRepository) {

  fun getUserStats(wallet: String): Single<GamificationStats> {
    return repository.getUserStats(wallet)
  }

  fun getLevels(wallet: String): Single<Levels> {
    return repository.getLevels(wallet)
  }

  fun getUserBonusAndLevel(wallet: String): Single<ForecastBonusAndLevel> {
    return repository.getUserStatus(wallet)
        .map { map(it) }
        .onErrorReturn { mapReferralError(it) }
  }

  private fun map(userStatusResponse: UserStatusResponse): ForecastBonusAndLevel {
    val gamificationResponse = userStatusResponse.gamification
    val referralResponse = userStatusResponse.referral
    if (referralResponse.pendingAmount.compareTo(BigDecimal.ZERO) == 0) {
      return ForecastBonusAndLevel(status = ForecastBonus.Status.INACTIVE,
          level = gamificationResponse.level)
    }
    return ForecastBonusAndLevel(
        ForecastBonus.Status.ACTIVE,
        referralResponse.pendingAmount,
        minAmount = referralResponse.minAmount,
        level = gamificationResponse.level)
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

  fun getLastShownLevel(wallet: String, screen: String): Single<Int> {
    return repository.getLastShownLevel(wallet, screen)
  }
}
