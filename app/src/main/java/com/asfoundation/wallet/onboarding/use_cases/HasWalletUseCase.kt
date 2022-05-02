package com.asfoundation.wallet.onboarding.use_cases

import com.asfoundation.wallet.ui.wallets.WalletsInteract
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class HasWalletUseCase @Inject constructor(private val walletsInteract: WalletsInteract) {

  operator fun invoke(): Single<Boolean> = walletsInteract.getWalletsModel()
    .subscribeOn(Schedulers.io())
    .map { it.totalWallets > 0 }
}
