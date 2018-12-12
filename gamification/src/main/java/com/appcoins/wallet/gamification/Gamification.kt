package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.ForecastBonus
import com.appcoins.wallet.gamification.repository.GamificationRepository
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import io.reactivex.Single
import java.math.BigDecimal

class Gamification(private val repository: GamificationRepository) {
  private var forecastBonus: ForecastBonus? = null
  fun getUserStatus(wallet: String): Single<UserStats> {
    return repository.getUserStatus(wallet)
  }

  fun getLevels(): Single<Levels> {
    return repository.getLevels()
  }

  fun getEarningBonus(wallet: String, packageName: String,
                      amount: BigDecimal): Single<ForecastBonus> {
    if (forecastBonus == null) {
      return repository.getForecastBonus(wallet, packageName, amount)
          .doOnSuccess { this.forecastBonus = it }
    }
    return Single.just(forecastBonus)
  }
}
