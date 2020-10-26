package com.asfoundation.wallet.ui.gamification

import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.ForecastBonus
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.appcoins.wallet.gamification.repository.GamificationStats
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.entity.GamificationResponse
import com.appcoins.wallet.gamification.repository.entity.PromotionsResponse
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.service.LocalCurrencyConversionService
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function3
import java.math.BigDecimal

class GamificationInteractor(
    private val gamification: Gamification,
    private val defaultWallet: FindDefaultWalletInteract,
    private val conversionService: LocalCurrencyConversionService,
    private val gamificationMapper: GamificationMapper) {

  private var isBonusActiveAndValid: Boolean = false

  fun getLevels(): Single<Levels> {
    return defaultWallet.find()
        .flatMap { gamification.getLevels(it.address) }
  }

  fun getUserStats(): Single<GamificationStats> {
    return defaultWallet.find()
        .flatMap { gamification.getUserStats(it.address) }
  }

  fun getEarningBonus(packageName: String, amount: BigDecimal): Single<ForecastBonusAndLevel> {
    return defaultWallet.find()
        .flatMap { wallet: Wallet ->
          Single.zip(
              gamification.getEarningBonus(wallet.address, packageName, amount),
              conversionService.localCurrency,
              gamification.getUserBonusAndLevel(wallet.address),
              Function3 { appcBonusValue: ForecastBonus, localCurrency: FiatValue, userBonusAndLevel: ForecastBonusAndLevel ->
                map(appcBonusValue, localCurrency, userBonusAndLevel, amount)
              })
        }
        .doOnSuccess { isBonusActiveAndValid = isBonusActiveAndValid(it) }
  }


  private fun map(forecastBonus: ForecastBonus, fiatValue: FiatValue,
                  forecastBonusAndLevel: ForecastBonusAndLevel,
                  amount: BigDecimal): ForecastBonusAndLevel {
    val status = getBonusStatus(forecastBonus, forecastBonusAndLevel)
    var bonus = forecastBonus.amount.multiply(fiatValue.amount)

    if (amount.multiply(fiatValue.amount) >= forecastBonusAndLevel.minAmount) {
      bonus = bonus.add(forecastBonusAndLevel.amount)
    }
    return ForecastBonusAndLevel(status, bonus, fiatValue.symbol,
        level = forecastBonusAndLevel.level)
  }

  private fun getBonusStatus(forecastBonus: ForecastBonus,
                             userBonusAndLevel: ForecastBonusAndLevel): ForecastBonus.Status {
    return if (forecastBonus.status == ForecastBonus.Status.ACTIVE || userBonusAndLevel.status == ForecastBonus.Status.ACTIVE) {
      ForecastBonus.Status.ACTIVE
    } else {
      ForecastBonus.Status.INACTIVE
    }
  }

  fun hasNewLevel(walletAddress: String,
                  gamificationResponse: GamificationResponse?,
                  screen: GamificationScreen): Single<Boolean> {
    return if (gamificationResponse == null || gamificationResponse.status != PromotionsResponse.Status.ACTIVE) {
      Single.just(false)
    } else {
      gamification.hasNewLevel(walletAddress, screen.toString())
    }
  }

  fun levelShown(level: Int, screen: GamificationScreen): Completable {
    return defaultWallet.find()
        .flatMapCompletable { gamification.levelShown(it.address, level, screen.toString()) }
  }

  fun getAppcToLocalFiat(value: String, scale: Int): Observable<FiatValue> {
    return conversionService.getAppcToLocalFiat(value, scale)
        .onErrorReturn { FiatValue(BigDecimal("-1"), "", "") }
  }

  fun isBonusActiveAndValid(): Boolean {
    return isBonusActiveAndValid
  }

  fun isBonusActiveAndValid(forecastBonus: ForecastBonusAndLevel): Boolean {
    return forecastBonus.status == ForecastBonus.Status.ACTIVE && forecastBonus.amount > BigDecimal.ZERO
  }


  private fun willLevelUp(userStats: GamificationStats, transactionValue: BigDecimal): Boolean {
    return userStats.totalSpend + transactionValue >= userStats.nextLevelAmount
  }

  private fun shouldShowNextLevel(levels: Levels, progress: Double, userStats: GamificationStats,
                                  gamificationLevel: Int): Boolean {
    return gamificationMapper.mapLevelUpPercentage(gamificationLevel) <= progress &&
        gamificationLevel < levels.list.size - 1 && levels.status == Levels.Status.OK && userStats.status == GamificationStats.Status.OK && userStats.nextLevelAmount != null
  }
}
