package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.*

class MergedAppcoinsInteract(private val balanceInteract: BalanceInteract,
                             private val walletBlockedInteract: WalletBlockedInteract,
                             private val supportInteractor: SupportInteractor,
                             private val walletService: WalletService) {

  fun showSupport(gamificationLevel: Int): Completable {
    return walletService.getWalletAddress()
        .flatMapCompletable {
          Completable.fromAction {
            supportInteractor.registerUser(gamificationLevel, it.toLowerCase(Locale.ROOT))
            supportInteractor.displayChatScreen()
          }
        }
  }

  fun getEthBalance(): Observable<FiatValue> = balanceInteract.getEthBalance()
      .map { it.second }

  fun getAppcBalance(): Observable<FiatValue> = balanceInteract.getAppcBalance()
      .map { it.second }

  fun getCreditsBalance(): Observable<FiatValue> =
      balanceInteract.getCreditsBalance()
          .map { it.second }

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()
}