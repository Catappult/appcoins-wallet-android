package com.asfoundation.wallet.wallets.usecases

import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.ui.wallets.WalletsModel
import io.reactivex.Observable
import javax.inject.Inject

class ObserveWalletsModelUseCase @Inject constructor(
  private val walletsInteract: WalletsInteract,
  private val rxSchedulers: RxSchedulers
) {

  operator fun invoke(): Observable<WalletsModel> = walletsInteract.observeWalletsModel()
    .subscribeOn(rxSchedulers.io)
}