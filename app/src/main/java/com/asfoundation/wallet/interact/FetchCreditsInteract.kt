package com.asfoundation.wallet.interact

import com.asfoundation.wallet.entity.Wallet
import io.reactivex.Observable
import java.math.BigDecimal

class FetchCreditsInteract (private val balanceGetter: BalanceGetter) {
  fun getBalance(wallet: Wallet): Observable<BigDecimal> {
    return balanceGetter.getBalance(wallet.address).toObservable()
  }
}