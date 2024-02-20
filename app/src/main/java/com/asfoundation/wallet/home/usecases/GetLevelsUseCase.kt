package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.Levels
import io.reactivex.Single
import javax.inject.Inject

class GetLevelsUseCase
@Inject
constructor(
    private val gamification: Gamification,
    private val findDefaultWalletUseCase: FindDefaultWalletUseCase
) {

  operator fun invoke(): Single<Levels> {
    return findDefaultWalletUseCase()
        .flatMapObservable { gamification.getLevels(it.address, offlineFirst = false) }
        .lastOrError()
  }
}
