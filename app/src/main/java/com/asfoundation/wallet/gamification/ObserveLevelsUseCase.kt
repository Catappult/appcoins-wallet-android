package com.asfoundation.wallet.gamification

import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import io.reactivex.Observable
import javax.inject.Inject

class ObserveLevelsUseCase @Inject constructor(
    private val getCurrentWallet: GetCurrentWalletUseCase,
    private val gamification: Gamification) {

  operator fun invoke(): Observable<Levels> {
    return getCurrentWallet()
        .flatMapObservable { gamification.getLevels(it.address, true) }
  }
}