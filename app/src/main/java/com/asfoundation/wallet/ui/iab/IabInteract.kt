package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.core.analytics.analytics.logging.Log
import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.backup.NotificationNeeded
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.wallet.appcoins.feature.support.data.SupportInteractor
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class IabInteract @Inject constructor(private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                                      private val supportInteractor: SupportInteractor,
                                      private val gamificationRepository: Gamification,
                                      private val walletBlockedInteract: WalletBlockedInteract,
                                      private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase) {

  companion object {
    const val PRE_SELECTED_PAYMENT_METHOD_KEY = "PRE_SELECTED_PAYMENT_METHOD_KEY"
  }

  fun showSupport(): Completable = supportInteractor.showSupport()

  fun hasPreSelectedPaymentMethod() = inAppPurchaseInteractor.hasPreSelectedPaymentMethod()

  fun getPreSelectedPaymentMethod(): String = inAppPurchaseInteractor.preSelectedPaymentMethod

  fun getWalletAddress(): Single<String> = inAppPurchaseInteractor.walletAddress

  fun registerUser() =
      inAppPurchaseInteractor.walletAddress.flatMap { address ->
        getCurrentPromoCodeUseCase()
            .flatMap { promoCode ->
              gamificationRepository.getUserLevel(address, promoCode.code)
                  .doOnSuccess { supportInteractor.registerUser(it, address) }
            }
      }

  fun savePreSelectedPaymentMethod(paymentMethod: String) {
    inAppPurchaseInteractor.savePreSelectedPaymentMethod(paymentMethod)
  }

  fun incrementAndValidateNotificationNeeded(): Single<NotificationNeeded> =
      inAppPurchaseInteractor.incrementAndValidateNotificationNeeded()

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()
}