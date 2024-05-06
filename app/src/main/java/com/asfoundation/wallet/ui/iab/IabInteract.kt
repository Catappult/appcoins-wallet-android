package com.asfoundation.wallet.ui.iab

import com.asfoundation.wallet.backup.NotificationNeeded
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.wallet.appcoins.feature.support.data.SupportInteractor
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class IabInteract @Inject constructor(
  private val inAppPurchaseInteractor: InAppPurchaseInteractor,
  private val supportInteractor: SupportInteractor,
  private val walletBlockedInteract: WalletBlockedInteract,
) {

  companion object {
    const val PRE_SELECTED_PAYMENT_METHOD_KEY = "PRE_SELECTED_PAYMENT_METHOD_KEY"
  }

  fun showSupport(): Completable = supportInteractor.showSupport()

  fun hasPreSelectedPaymentMethod() = inAppPurchaseInteractor.hasPreSelectedPaymentMethod()

  fun getPreSelectedPaymentMethod(): String = inAppPurchaseInteractor.preSelectedPaymentMethod

  fun getWalletAddress(): Single<String> = inAppPurchaseInteractor.walletAddress

  fun savePreSelectedPaymentMethod(paymentMethod: String) {
    inAppPurchaseInteractor.savePreSelectedPaymentMethod(paymentMethod)
  }

  fun incrementAndValidateNotificationNeeded(): Single<NotificationNeeded> =
    inAppPurchaseInteractor.incrementAndValidateNotificationNeeded()

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()
}