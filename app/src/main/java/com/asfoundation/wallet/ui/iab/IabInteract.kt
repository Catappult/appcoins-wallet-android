package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.backup.NotificationNeeded
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Single

class IabInteract(private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                  private val autoUpdateInteract: AutoUpdateInteract,
                  private val supportInteractor: SupportInteractor,
                  private val gamificationRepository: Gamification,
                  private val walletBlockedInteract: WalletBlockedInteract) {

  companion object {
    const val PRE_SELECTED_PAYMENT_METHOD_KEY = "PRE_SELECTED_PAYMENT_METHOD_KEY"
  }

  fun showSupport() = supportInteractor.displayChatScreen()

  fun hasPreSelectedPaymentMethod() = inAppPurchaseInteractor.hasPreSelectedPaymentMethod()

  fun getPreSelectedPaymentMethod(): String = inAppPurchaseInteractor.preSelectedPaymentMethod

  fun getWalletAddress(): Single<String> = inAppPurchaseInteractor.walletAddress

  fun getAutoUpdateModel(invalidateCache: Boolean = true) =
      autoUpdateInteract.getAutoUpdateModel(invalidateCache)

  fun isHardUpdateRequired(blackList: List<Int>, updateVersionCode: Int, updateMinSdk: Int) =
      autoUpdateInteract.isHardUpdateRequired(blackList, updateVersionCode, updateMinSdk)

  fun registerUser() =
      inAppPurchaseInteractor.walletAddress.flatMap { address ->
        gamificationRepository.getUserLevel(address)
            .doOnSuccess { supportInteractor.registerUser(it, address) }
      }

  fun savePreSelectedPaymentMethod(paymentMethod: String) {
    inAppPurchaseInteractor.savePreSelectedPaymentMethod(paymentMethod)
  }

  fun incrementAndValidateNotificationNeeded(): Single<NotificationNeeded> =
      inAppPurchaseInteractor.incrementAndValidateNotificationNeeded()

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()
}