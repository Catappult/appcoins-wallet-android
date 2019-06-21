package com.asfoundation.wallet.ui.balance

import android.util.Pair
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable

interface BalanceRepository {
  fun getEthBalance(wallet: Wallet): Observable<Pair<Balance, FiatValue>>

  fun getAppcBalance(wallet: Wallet): Observable<Pair<Balance, FiatValue>>

  fun getCreditsBalance(wallet: Wallet): Observable<Pair<Balance, FiatValue>>
}