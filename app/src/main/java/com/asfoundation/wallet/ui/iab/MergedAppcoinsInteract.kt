package com.asfoundation.wallet.ui.iab

import android.util.Pair
import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class MergedAppcoinsInteract(private val balanceInteract: BalanceInteract,
                             private val walletBlockedInteract: WalletBlockedInteract,
                             private val supportInteractor: SupportInteractor,
                             private val walletService: WalletService) {

  fun showSupport(gamificationLevel: Int): Completable {
    return walletService.getWalletAddress()
        .flatMapCompletable {
          Completable.fromAction {
            supportInteractor.registerUser(gamificationLevel, it.toLowerCase())
            supportInteractor.displayChatScreen()
          }
        }
  }

  fun getEthBalance(): Observable<Pair<Balance, FiatValue>> {
    return balanceInteract.getEthBalance()

  }

  fun getAppcBalance(): Observable<Pair<Balance, FiatValue>> {
    return balanceInteract.getAppcBalance()

  }

  fun getCreditsBalance(): Observable<Pair<Balance, FiatValue>> {
    return balanceInteract.getCreditsBalance()
  }

  fun isWalletBlocked(): Single<Boolean> {
    return walletBlockedInteract.isWalletBlocked()
  }
}