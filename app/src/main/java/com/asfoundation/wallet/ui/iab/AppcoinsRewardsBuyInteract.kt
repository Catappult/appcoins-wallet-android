package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

class AppcoinsRewardsBuyInteract(private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                                 private val supportRepository: SupportRepository,
                                 private val walletService: WalletService,
                                 private val walletBlockedInteract: WalletBlockedInteract,
                                 private val smsValidationInteract: SmsValidationInteract) {

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()

  fun isWalletVerified() =
      walletService.getWalletAddress()
          .flatMap { smsValidationInteract.isValidated(it) }
          .onErrorReturn { true }

  fun showSupport(gamificationLevel: Int): Completable {
    return walletService.getWalletAddress()
        .flatMapCompletable {
          Completable.fromAction {
            supportRepository.registerUser(gamificationLevel, it.toLowerCase(Locale.ROOT))
            supportRepository.displayChatScreen()
          }
        }
  }

  fun removeAsyncLocalPayment() = inAppPurchaseInteractor.removeAsyncLocalPayment()

  fun convertToFiat(appcValue: Double, currency: String): Single<FiatValue> =
      inAppPurchaseInteractor.convertToFiat(appcValue, currency)
}