package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.backup.NotificationNeeded
import com.wallet.appcoins.feature.support.data.SupportInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Single
import javax.inject.Inject

class IabInteract @Inject constructor(private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                                      private val supportInteractor: com.wallet.appcoins.feature.support.data.SupportInteractor,
                                      private val gamificationRepository: Gamification,
                                      private val walletBlockedInteract: WalletBlockedInteract,
                                      private val getCurrentPromoCodeUseCase: com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase) {

  companion object {
    const val PRE_SELECTED_PAYMENT_METHOD_KEY = "PRE_SELECTED_PAYMENT_METHOD_KEY"
  }

  fun showSupport() = supportInteractor.displayChatScreen()

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