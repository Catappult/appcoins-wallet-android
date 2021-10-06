package com.asfoundation.wallet.ui.iab

import android.util.Pair
import com.asf.wallet.R
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.asfoundation.wallet.wallets.GetDefaultWalletBalanceInteract.BalanceState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class MergedAppcoinsInteractor(private val balanceInteractor: BalanceInteractor,
                               private val walletBlockedInteract: WalletBlockedInteract,
                               private val supportInteractor: SupportInteractor,
                               private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                               private val fingerprintPreferences: FingerprintPreferencesRepositoryContract) {

  fun showSupport(gamificationLevel: Int): Completable {
    return supportInteractor.showSupport(gamificationLevel)
  }

  fun getEthBalance(): Observable<FiatValue> = balanceInteractor.getEthBalance()
      .map { it.second }

  fun getAppcBalance(): Observable<FiatValue> = balanceInteractor.getAppcBalance()
      .map { it.second }

  fun getCreditsBalance(): Observable<Pair<Balance, FiatValue>> =
      balanceInteractor.getCreditsBalance()

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()

  fun retrieveAppcAvailability(transactionBuilder: TransactionBuilder,
                               isSubscription: Boolean): Single<Availability> {
    return if (isSubscription) {
      //TODO replace for correct string
      // Note that currently this is not available (only Adyen is available for subscriptions)
      // Since it is not available server-side, these developments don't really matter right now.
      // We should revisit this if there ever is support for subscriptions with APPC
      Single.just(Availability(false, R.string.subscriptions_details_disclaimer))
    } else {
      inAppPurchaseInteractor.getBalanceState(transactionBuilder)
          .map {
            when (it) {
              BalanceState.NO_ETHER -> Availability(false, R.string.purchase_no_eth_body)
              BalanceState.NO_TOKEN, BalanceState.NO_ETHER_NO_TOKEN -> Availability(false,
                  R.string.purchase_no_appcoins_body)
              BalanceState.OK -> Availability(true, null)
            }
          }
    }
  }

  fun hasAuthenticationPermission() = fingerprintPreferences.hasAuthenticationPermission()
}