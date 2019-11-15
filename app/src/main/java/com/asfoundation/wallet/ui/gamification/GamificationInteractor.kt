package com.asfoundation.wallet.ui.gamification

import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.ForecastBonus
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.service.LocalCurrencyConversionService
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function4
import java.math.BigDecimal

class GamificationInteractor(
    private val gamification: Gamification,
    private val defaultWallet: FindDefaultWalletInteract,
    private val conversionService: LocalCurrencyConversionService) {

  fun getLevels(): Single<Levels> {
    return gamification.getLevels()
  }

  fun getUserStats(): Single<UserStats> {
    return defaultWallet.find()
        .flatMap { gamification.getUserStats(it.address) }
  }

  fun getEarningBonus(packageName: String, amount: BigDecimal): Single<ForecastBonus> {
    return defaultWallet.find()
        .flatMap { wallet: Wallet ->
          Single.zip(gamification.getEarningBonus(wallet.address, packageName, amount),
              conversionService.localCurrency,
              gamification.getNextPurchaseBonus(wallet.address),
              gamification.getReferralsUserStatus(wallet.address),
              Function4 { appcBonusValue: ForecastBonus, localCurrency: FiatValue,
                          referralBonus: ForecastBonus, referralsInfo: ReferralResponse ->
                map(appcBonusValue, localCurrency, referralBonus, referralsInfo, amount)
              })
        }
  }

  private fun map(forecastBonus: ForecastBonus, fiatValue: FiatValue,
                  referralBonus: ForecastBonus, referralsInfo: ReferralResponse,
                  amount: BigDecimal): ForecastBonus {
    val status = getBonusStatus(forecastBonus, referralBonus)
    var bonus = forecastBonus.amount.multiply(fiatValue.amount)

    if (amount.multiply(fiatValue.amount) >= referralsInfo.minAmount) {
      bonus = bonus.add(referralBonus.amount)
    }
    return ForecastBonus(status, bonus, fiatValue.symbol)
  }

  private fun getBonusStatus(forecastBonus: ForecastBonus,
                             referralBonus: ForecastBonus): ForecastBonus.Status {
    return if (forecastBonus.status == ForecastBonus.Status.ACTIVE || referralBonus.status == ForecastBonus.Status.ACTIVE) {
      ForecastBonus.Status.ACTIVE
    } else {
      ForecastBonus.Status.INACTIVE
    }
  }

  fun hasNewLevel(screen: GamificationScreen): Single<Boolean> {
    return defaultWallet.find()
        .flatMap { gamification.hasNewLevel(it.address, screen.toString()) }
  }

  fun levelShown(level: Int, screen: GamificationScreen): Completable {
    return defaultWallet.find()
        .flatMapCompletable { gamification.levelShown(it.address, level, screen.toString()) }
  }

  fun getLastShownLevel(screen: GamificationScreen): Single<Int> {
    return defaultWallet.find()
        .flatMap { gamification.getLastShownLevel(it.address, screen.toString()) }
  }

  fun getAppcToLocalFiat(value: String, scale: Int): Observable<FiatValue> {
    return conversionService.getAppcToLocalFiat(value, scale)
        .onErrorReturn { FiatValue(BigDecimal("-1"), "", "") }
  }
}
