package com.asfoundation.wallet.ui.gamification

import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import io.reactivex.Single

class GamificationInteractor(private val gamification: Gamification,
                             private val defaultWallet: FindDefaultWalletInteract) {
  fun getLevels(): Single<Levels> {
    return gamification.getLevels()
  }

  fun getUserStatus(): Single<UserStats> {
    return defaultWallet.find().flatMap { gamification.getUserStatus(it.address) }
  }
}
