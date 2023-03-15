package com.asfoundation.wallet.onboarding.use_cases

import com.appcoins.wallet.ui.arch.RxSchedulers
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import io.reactivex.Single
import javax.inject.Inject

class HasWalletUseCase @Inject constructor(
  private val walletsInteract: WalletsInteract,
  private val rxSchedulers: com.appcoins.wallet.ui.arch.RxSchedulers
) {

  operator fun invoke(): Single<Boolean> = walletsInteract.getWalletsModel()
    .subscribeOn(rxSchedulers.io)
    .map { it.totalWallets > 0 }
}
