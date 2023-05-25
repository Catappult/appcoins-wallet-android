package com.asfoundation.wallet.wallet_blocked

import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WalletBlockedInteract @Inject constructor(
    private val getWalletInfoUseCase: GetWalletInfoUseCase) {

  fun isWalletBlocked(): Single<Boolean> {
    return getWalletInfoUseCase(null, cached = false, updateFiat = false)
        .map { walletInfo -> walletInfo.blocked }
        .onErrorReturn { false }
        .delay(1, TimeUnit.SECONDS)
  }
}