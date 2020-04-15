package com.asfoundation.wallet.ui.balance

import android.util.Pair
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable
import io.reactivex.Single

interface BalanceRepository {
  fun getEthBalance(wallet: Wallet): Observable<Pair<Balance, FiatValue>>

  fun getAppcBalance(wallet: Wallet): Observable<Pair<Balance, FiatValue>>

  fun getCreditsBalance(wallet: Wallet): Observable<Pair<Balance, FiatValue>>

  fun getStoredEthBalance(walletAddr: String): Single<Pair<Balance, FiatValue>>

  fun getStoredAppcBalance(walletAddr: String): Single<Pair<Balance, FiatValue>>

  fun getStoredCreditsBalance(walletAddr: String): Single<Pair<Balance, FiatValue>>

}