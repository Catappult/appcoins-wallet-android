package com.asfoundation.wallet.ui.iab

import android.util.Pair
import com.appcoins.wallet.bdsbilling.WalletService
import com.asf.wallet.R
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.interact.GetDefaultWalletBalanceInteract.BalanceState
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

class MergedAppcoinsInteractor(private val balanceInteractor: BalanceInteractor,
                               private val walletBlockedInteract: WalletBlockedInteract,
                               private val supportRepository: SupportRepository,
                               private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                               private val walletService: WalletService,
                               private val preferencesRepositoryType: PreferencesRepositoryType) {

  fun showSupport(gamificationLevel: Int): Completable {
    return walletService.getWalletAddress()
        .flatMapCompletable {
          Completable.fromAction {
            supportRepository.registerUser(gamificationLevel, it.toLowerCase(Locale.ROOT))
            supportRepository.displayChatScreen()
          }
        }
  }

  fun getEthBalance(): Observable<FiatValue> = balanceInteractor.getEthBalance()
      .map { it.second }

  fun getAppcBalance(): Observable<FiatValue> = balanceInteractor.getAppcBalance()
      .map { it.second }

  fun getCreditsBalance(): Observable<Pair<Balance, FiatValue>> =
      balanceInteractor.getCreditsBalance()

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()

  fun hasAppcFunds(transactionBuilder: TransactionBuilder): Single<Availability> {
    return inAppPurchaseInteractor.getBalanceState(transactionBuilder)
        .map {
          when (it) {
            BalanceState.NO_ETHER -> Availability(false, R.string.purchase_no_eth_body)
            BalanceState.NO_TOKEN, BalanceState.NO_ETHER_NO_TOKEN -> Availability(false,
                R.string.purchase_no_appcoins_body)
            BalanceState.OK -> Availability(true, null)
          }
        }
  }

  fun hasAuthenticationPermission() = preferencesRepositoryType.hasAuthenticationPermission()
}