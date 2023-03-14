package com.asfoundation.wallet.ui.iab

import com.asf.wallet.R
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.repository.InAppPurchaseService
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.appcoins.wallet.sharedpreferences.FingerprintPreferencesDataSource
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class MergedAppcoinsInteractor @Inject constructor(
  private val balanceInteractor: BalanceInteractor,
  private val walletBlockedInteract: WalletBlockedInteract,
  private val supportInteractor: SupportInteractor,
  private val inAppPurchaseInteractor: InAppPurchaseInteractor,
  private val fingerprintPreferences: FingerprintPreferencesDataSource
) {

  fun showSupport(gamificationLevel: Int): Completable {
    return supportInteractor.showSupport(gamificationLevel)
  }

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()

  fun retrieveAppcAvailability(
    transactionBuilder: TransactionBuilder,
    isSubscription: Boolean
  ): Single<Availability> {
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
              InAppPurchaseService.BalanceState.NO_ETHER -> Availability(false,
                  R.string.purchase_no_eth_body)
              InAppPurchaseService.BalanceState.NO_TOKEN, InAppPurchaseService.BalanceState.NO_ETHER_NO_TOKEN -> Availability(
                  false, R.string.purchase_no_appcoins_body)
              InAppPurchaseService.BalanceState.OK -> Availability(true, null)
            }
          }
    }
  }

  fun hasAuthenticationPermission() = fingerprintPreferences.hasAuthenticationPermission()
}