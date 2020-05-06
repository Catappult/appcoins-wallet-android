package com.asfoundation.wallet.ui.balance

import android.util.Pair
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable
import io.reactivex.Single

interface BalanceRepository {
  fun getEthBalance(address: String): Observable<Pair<Balance, FiatValue>>

  fun getAppcBalance(address: String): Observable<Pair<Balance, FiatValue>>

  fun getCreditsBalance(address: String): Observable<Pair<Balance, FiatValue>>

  fun getStoredEthBalance(walletAddress: String): Single<Pair<Balance, FiatValue>>

  fun getStoredAppcBalance(walletAddress: String): Single<Pair<Balance, FiatValue>>

  fun getStoredCreditsBalance(walletAddress: String): Single<Pair<Balance, FiatValue>>
}