package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.support.SupportInteractor
import io.reactivex.Completable
import io.reactivex.Single

class AppcoinsRewardsBuyInteract(private val inAppPurchaseInteractor: InAppPurchaseInteractor,
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

  fun removeAsyncLocalPayment() = inAppPurchaseInteractor.removeAsyncLocalPayment()

  fun convertToFiat(appcValue: Double, currency: String): Single<FiatValue> =
      inAppPurchaseInteractor.convertToFiat(appcValue, currency)
}