package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.ForecastBonus
import com.appcoins.wallet.gamification.repository.GamificationRepository
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.net.UnknownHostException

class Gamification(private val repository: GamificationRepository) {

  fun getUserStatus(wallet: String): Single<UserStats> {
    return repository.getUserStats(wallet)
  }

  fun getLevels(): Single<Levels> {
    return repository.getLevels()
  }

  fun getNextPurchaseBonus(wallet: String): Single<ForecastBonus> {
    return repository.getReferralUserStatus(wallet)
        .map { map(it) }
        .onErrorReturn { mapReferralError(it) }
  }

  private fun map(referralResponse: ReferralResponse?): ForecastBonus {
    if (referralResponse == null || referralResponse.pendingValue.compareTo(
            BigDecimal.ZERO) == 0) {
      return ForecastBonus(ForecastBonus.Status.INACTIVE)
    }
    return ForecastBonus(ForecastBonus.Status.ACTIVE, referralResponse.pendingValue)
  }

  private fun mapReferralError(throwable: Throwable): ForecastBonus {
    throwable.printStackTrace()
    return when (throwable) {
      is UnknownHostException -> ForecastBonus(ForecastBonus.Status.NO_NETWORK)
      else -> {
        ForecastBonus(ForecastBonus.Status.UNKNOWN_ERROR)
      }
    }
  }

  fun getEarningBonus(wallet: String, packageName: String,
                      amount: BigDecimal): Single<ForecastBonus> {
    return repository.getForecastBonus(wallet, packageName, amount)
  }

  fun hasNewLevel(wallet: String, screen: String): Single<Boolean> {
    return Single.zip(repository.getLastShownLevel(wallet, screen), getUserStatus(wallet),
        BiFunction { lastShownLevel: Int, userStats: UserStats ->
          userStats.status == UserStats.Status.OK && lastShownLevel < userStats.level
        })
  }

  fun levelShown(wallet: String, level: Int, screen: String): Completable {
    return repository.shownLevel(wallet, level, screen)
  }

  fun getLastShownLevel(wallet: String, screen: String): Single<Int> {
    return repository.getLastShownLevel(wallet, screen)
  }
}
