package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.verification.ui.credit_card.WalletVerificationInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class AppcoinsRewardsBuyInteract @Inject constructor(
    private val inAppPurchaseInteractor: InAppPurchaseInteractor,
    private val supportInteractor: SupportInteractor,
    private val walletService: WalletService,
    private val walletBlockedInteract: WalletBlockedInteract,
    private val walletVerificationInteractor: WalletVerificationInteractor) {

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()

  fun isWalletVerified() =
      walletService.getAndSignCurrentWalletAddress()
          .flatMap { walletVerificationInteractor.isVerified(it.address, it.signedAddress) }
          .onErrorReturn { true }

  fun showSupport(gamificationLevel: Int): Completable {
    return supportInteractor.showSupport(gamificationLevel)
  }

  fun removeAsyncLocalPayment() = inAppPurchaseInteractor.removeAsyncLocalPayment()

  fun convertToFiat(appcValue: Double, currency: String): Single<com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue> =
      inAppPurchaseInteractor.convertToFiat(appcValue, currency)
}