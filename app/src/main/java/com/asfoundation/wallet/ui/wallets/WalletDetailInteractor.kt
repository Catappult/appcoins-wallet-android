package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.balance.BalanceScreenModel
import io.reactivex.Observable

class WalletDetailInteractor(private val balanceInteract: BalanceInteract) {

  fun getBalanceModel(address: String): Observable<BalanceScreenModel> {
    return balanceInteract.requestTokenConversion(address)
        .take(1)
  }

}
