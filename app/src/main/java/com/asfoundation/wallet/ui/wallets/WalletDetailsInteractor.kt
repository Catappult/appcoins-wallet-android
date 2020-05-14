package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.interact.SetDefaultWalletInteract
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.balance.BalanceScreenModel
import io.reactivex.Completable
import io.reactivex.Single

class WalletDetailsInteractor(private val balanceInteract: BalanceInteract,
                              private val setDefaultWalletInteract: SetDefaultWalletInteract) {

  fun getBalanceModel(address: String): Single<BalanceScreenModel> =
      balanceInteract.getStoredBalanceScreenModel(address)

  fun setActiveWallet(address: String): Completable = setDefaultWalletInteract.set(address)

}
