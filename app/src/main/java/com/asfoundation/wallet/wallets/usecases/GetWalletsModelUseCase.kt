package com.asfoundation.wallet.wallets.usecases

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.ui.wallets.WalletsModel
import io.reactivex.Single
import javax.inject.Inject

class GetWalletsModelUseCase @Inject constructor(
  private val walletsInteract: WalletsInteract,
  private val rxSchedulers: RxSchedulers
) {

  operator fun invoke(): Single<WalletsModel> = walletsInteract.getWalletsModel()
    .subscribeOn(rxSchedulers.io)
}