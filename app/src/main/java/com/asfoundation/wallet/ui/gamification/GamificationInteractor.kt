package com.asfoundation.wallet.ui.gamification

import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.ForecastBonus
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.appcoins.wallet.gamification.repository.GamificationStats
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.entity.GamificationResponse
import com.appcoins.wallet.gamification.repository.entity.PromotionsResponse
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function3
import java.math.BigDecimal

class GamificationInteractor(
    private val gamification: Gamification,
    private val defaultWallet: FindDefaultWalletInteract,
    private val conversionService: LocalCurrencyConversionService) {

  private var isBonusActiveAndValid: Boolean = false

  fun getLevels(offlineFirst: Boolean = true): Observable<Levels> {
    return defaultWallet.find()
        .flatMapObservable { gamification.getLevels(it.address, offlineFirst) }
  }

  fun getUserStats(): Observable<GamificationStats> {
    return defaultWallet.find()
        .flatMapObservable { gamification.getUserStats(it.address) }
  }

  fun getUserLevel(): Single<Int> {
    return defaultWallet.find()
        .flatMap { gamification.getUserLevel(it.address) }
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
                  gamificationContext: GamificationContext): Single<Boolean> {
    return if (gamificationResponse == null || gamificationResponse.status != PromotionsResponse.Status.ACTIVE) {
      Single.just(false)
    } else {
      gamification.hasNewLevel(walletAddress, gamificationContext, gamificationResponse.level)
    }
  }

  fun levelShown(level: Int, gamificationContext: GamificationContext): Completable {
    return defaultWallet.find()
        .flatMapCompletable { gamification.levelShown(it.address, level, gamificationContext) }
  }

  fun getAppcToLocalFiat(value: String, scale: Int,
                         getFromCache: Boolean = false): Observable<FiatValue> {
    return conversionService.getAppcToLocalFiat(value, scale, getFromCache)
        .onErrorReturn { FiatValue(BigDecimal("-1"), "", "") }
  }

  fun isBonusActiveAndValid(): Boolean {
    return isBonusActiveAndValid
  }

  fun isBonusActiveAndValid(forecastBonus: ForecastBonusAndLevel): Boolean {
    return forecastBonus.status == ForecastBonus.Status.ACTIVE && forecastBonus.amount > BigDecimal.ZERO
  }
}
