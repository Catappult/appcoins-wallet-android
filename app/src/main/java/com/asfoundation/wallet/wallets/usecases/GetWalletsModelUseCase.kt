package com.asfoundation.wallet.wallets.usecases

import com.appcoins.wallet.ui.arch.RxSchedulers
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.ui.wallets.WalletsModel
import io.reactivex.Single
import javax.inject.Inject

class GetWalletsModelUseCase @Inject constructor(
  private val walletsInteract: WalletsInteract,
  private val rxSchedulers: com.appcoins.wallet.ui.arch.RxSchedulers
) {

  operator fun invoke(): Single<WalletsModel> = walletsInteract.getWalletsModel()
    .subscribeOn(rxSchedulers.io)
}