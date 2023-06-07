package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletsInteract
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import io.reactivex.Single
import javax.inject.Inject

class GetWalletsModelUseCase @Inject constructor(
    private val walletsInteract: WalletsInteract,
    private val rxSchedulers: RxSchedulers
) {

  operator fun invoke(): Single<WalletsModel> = walletsInteract.getWalletsModel()
    .subscribeOn(rxSchedulers.io)
}