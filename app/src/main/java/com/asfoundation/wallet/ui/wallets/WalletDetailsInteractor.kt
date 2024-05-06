package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.interact.SetDefaultWalletInteractor
import io.reactivex.Completable
import javax.inject.Inject

class WalletDetailsInteractor @Inject constructor(
  private val setDefaultWalletInteractor: SetDefaultWalletInteractor
) {

  fun setActiveWallet(address: String): Completable = setDefaultWalletInteractor.set(address)
}
