package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletsInteract
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import io.reactivex.Observable
import javax.inject.Inject

class ObserveWalletsModelUseCase @Inject constructor(
    private val walletsInteract: WalletsInteract,
    private val rxSchedulers: RxSchedulers
) {

  operator fun invoke(): Observable<WalletsModel> = walletsInteract.observeWalletsModel()
    .subscribeOn(rxSchedulers.io)
}