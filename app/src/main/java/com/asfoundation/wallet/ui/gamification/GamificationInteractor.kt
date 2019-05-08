package com.asfoundation.wallet.ui.gamification

import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.ForecastBonus
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.service.LocalCurrencyConversionService
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.math.BigDecimal

class GamificationInteractor(
    private val gamification: Gamification,
    private val defaultWallet: FindDefaultWalletInteract,
    private val conversionService: LocalCurrencyConversionService) {
  fun getLevels(): Single<Levels> {
    return gamification.getLevels()
  }

  fun getUserStatus(): Single<UserStats> {
    return defaultWallet.find().flatMap { gamification.getUserStatus(it.address) }
  }

  fun getEarningBonus(packageName: String, amount: BigDecimal): Single<ForecastBonus> {
    return Single.zip(defaultWallet.find()
        .flatMap { gamification.getEarningBonus(it.address, packageName, amount) },
        conversionService.getLocalCurrency(),
        BiFunction { appcBonusValue, localCurrency ->
          ForecastBonus(appcBonusValue.status,
              appcBonusValue.amount.multiply(localCurrency.amount), localCurrency.symbol)
        })
  }

  fun hasNewLevel(): Single<Boolean> {
    return defaultWallet.find().flatMap { gamification.hasNewLevel(it.address) }
  }

  fun levelShown(level: Int): Completable {
    return defaultWallet.find().flatMapCompletable { gamification.levelShown(it.address, level) }
  }

  fun getLastShownLevel(): Single<Int> {
    return defaultWallet.find().flatMap { gamification.getLastShownLevel(it.address) }
  }
}
