package com.asfoundation.wallet.ui.login.usecases

import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.ObserveWalletInfoUseCase
import io.reactivex.Single
import javax.inject.Inject

class IsCurrentWalletLoggedInUseCase @Inject constructor(
  private val observeWalletInfoUseCase: ObserveWalletInfoUseCase
) {
  operator fun invoke(): Single<Boolean> = observeWalletInfoUseCase(null, false)
    .firstOrError()
    .map { walletInfo ->
      walletInfo.email != null
    }.onErrorReturnItem(false)
}