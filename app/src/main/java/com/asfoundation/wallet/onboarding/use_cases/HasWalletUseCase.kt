package com.asfoundation.wallet.onboarding.use_cases

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import io.reactivex.Single
import javax.inject.Inject

class HasWalletUseCase @Inject constructor(
  private val walletsInteract: WalletsInteract,
  private val rxSchedulers: RxSchedulers
) {

  operator fun invoke(): Single<Boolean> = walletsInteract.getWalletsModel()
    .subscribeOn(rxSchedulers.io)
    .map { it.totalWallets > 0 }
}
