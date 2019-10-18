package com.asfoundation.wallet.ui.balance

import android.util.Pair
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable

class BalanceInteract(
    private val walletInteract: FindDefaultWalletInteract,
    private val balanceRepository: BalanceRepository) {

  fun getAppcBalance(): Observable<Pair<Balance, FiatValue>> {
    return walletInteract.find()
        .flatMapObservable { balanceRepository.getAppcBalance(it) }
  }

  fun getEthBalance(): Observable<Pair<Balance, FiatValue>> {
    return walletInteract.find()
        .flatMapObservable { balanceRepository.getEthBalance(it) }
  }

  fun getCreditsBalance(): Observable<Pair<Balance, FiatValue>> {
    return walletInteract.find()
        .flatMapObservable { balanceRepository.getCreditsBalance(it) }
  }
}
