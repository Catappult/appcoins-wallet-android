package com.asfoundation.wallet.ui.wallets

import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SetActiveWalletUseCase
import io.reactivex.Completable
import javax.inject.Inject

class WalletDetailsInteractor @Inject constructor(
  private val setActiveWalletUseCase: SetActiveWalletUseCase
) {

  fun setActiveWallet(address: String): Completable = setActiveWalletUseCase(address)
}
