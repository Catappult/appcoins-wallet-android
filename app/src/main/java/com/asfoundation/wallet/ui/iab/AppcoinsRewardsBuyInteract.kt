package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Single

class AppcoinsRewardsBuyInteract(private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                                 private val supportInteractor: SupportInteractor,
                                 private val walletService: WalletService,
                                 private val walletBlockedInteract: WalletBlockedInteract,
                                 private val smsValidationInteract: SmsValidationInteract) {

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()

  fun isWalletVerified() =
      walletService.getWalletAddress()
          .flatMap { smsValidationInteract.isValidated(it) }
          .onErrorReturn { true }

  fun showSupport(gamificationLevel: Int): Completable {
    return supportInteractor.showSupport(gamificationLevel)
  }

  fun removeAsyncLocalPayment() = inAppPurchaseInteractor.removeAsyncLocalPayment()

  fun convertToFiat(appcValue: Double, currency: String): Single<FiatValue> =
      inAppPurchaseInteractor.convertToFiat(appcValue, currency)
}