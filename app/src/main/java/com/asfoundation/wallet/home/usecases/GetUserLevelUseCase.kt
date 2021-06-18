package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.gamification.Gamification
import io.reactivex.Single

class GetUserLevelUseCase(private val gamification: Gamification,
                          private val findDefaultWalletUseCase: FindDefaultWalletUseCase) {

  operator fun invoke(): Single<Int> {
    return findDefaultWalletUseCase()
        .flatMap { gamification.getUserLevel(it.address) }
  }
}