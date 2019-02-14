package com.asfoundation.wallet.ui.gamification

import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.ForecastBonus
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import io.reactivex.Single
import java.math.BigDecimal

class GamificationInteractor(private val gamification: Gamification,
                             private val defaultWallet: FindDefaultWalletInteract) {
  fun getLevels(): Single<Levels> {
    return gamification.getLevels()
  }

  fun getUserStatus(): Single<UserStats> {
    return defaultWallet.find().flatMap { gamification.getUserStatus(it.address) }
  }

  fun getEarningBonus(packageName: String, amount: BigDecimal): Single<ForecastBonus> {
    return defaultWallet.find()
        .flatMap { gamification.getEarningBonus(it.address, packageName, amount) }
  }

  fun hasNewLevel(): Single<Boolean> {
    return defaultWallet.find().flatMap { gamification.hasNewLevel(it.address) }
  }
}
