package com.asfoundation.wallet.ui.balance

import android.util.Pair
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable

interface BalanceRepository {
  fun getEthBalance(address: String): Observable<Pair<Balance, FiatValue>>

  fun getAppcBalance(address: String): Observable<Pair<Balance, FiatValue>>

  fun getCreditsBalance(address: String): Observable<Pair<Balance, FiatValue>>
}