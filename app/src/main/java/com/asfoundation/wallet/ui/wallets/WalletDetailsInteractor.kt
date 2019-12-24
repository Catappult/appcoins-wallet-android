package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.interact.SetDefaultWalletInteract
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.balance.BalanceScreenModel
import io.reactivex.Completable
import io.reactivex.Observable

class WalletDetailsInteractor(private val balanceInteract: BalanceInteract,
                              private val setDefaultWalletInteract: SetDefaultWalletInteract) {

  fun getBalanceModel(address: String): Observable<BalanceScreenModel> {
    return balanceInteract.requestTokenConversion(address)
        .take(1)
  }

  fun setActiveWallet(address: String): Completable {
    return setDefaultWalletInteract.set(address)
  }

}
