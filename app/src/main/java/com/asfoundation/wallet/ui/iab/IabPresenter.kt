package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import androidx.annotation.StringRes
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.ui.iab.IabInteract.Companion.PRE_SELECTED_PAYMENT_METHOD_KEY
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class IabPresenter(private val view: IabView,
                   private val networkScheduler: Scheduler,
                   private val viewScheduler: Scheduler,
                   private val disposable: CompositeDisposable,
                   private val billingAnalytics: BillingAnalytics,
                   private var firstImpression: Boolean,
                   private val iabInteract: IabInteract,
                   private val walletBlockedInteract: WalletBlockedInteract,
                   private val logger: Logger) {

  companion object {
    private val TAG = IabActivity::class.java.name
  }

  fun present() {
    handleAutoUpdate()
    handleUserRegistration()
    handleSupportClicks()
    handleErrorDismisses()
  }

  private fun handleErrorDismisses() {
    disposable.add(view.errorDismisses()
        .doOnNext { view.close(Bundle()) }
        .subscribe({ }, { view.close(Bundle()) }))
  }

  private fun handleSupportClicks() {
    disposable.add(view.getSupportClicks()
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .doOnNext { iabInteract.showSupport() }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  fun handleWalletBlockedCheck(@StringRes error: Int) {
    disposable.add(
        walletBlockedInteract.isWalletBlocked()
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              if (it) view.showError(error)
              else view.showPaymentMethodsView()
            }
            .subscribe({}, { handleError(it) })
    )
  }

  private fun handleError(throwable: Throwable) {
    logger.log(TAG, throwable)
    view.finishWithError()
  }

  fun handlePerkNotifications(bundle: Bundle) {
    disposable.add(iabInteract.getWalletAddress()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          view.launchPerkBonusService(it)
          view.finishActivity(bundle)
        }
        .doOnError { view.finishActivity(bundle) }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun handleBackupNotifications(bundle: Bundle) {
    disposable.add(iabInteract.incrementAndValidateNotificationNeeded()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { notificationNeeded ->
          if (notificationNeeded.isNeeded) {
            view.showBackupNotification(notificationNeeded.walletAddress)
          }
          view.finishActivity(bundle)
        }
        .doOnError { view.finish(bundle) }
        .subscribe({ }, { it.printStackTrace() })
    )
  }

  fun handlePurchaseStartAnalytics(transaction: TransactionBuilder?) {
    disposable.add(Completable.fromAction {
      if (firstImpression) {
        if (iabInteract.hasPreSelectedPaymentMethod()) {
          billingAnalytics.sendPurchaseStartEvent(transaction?.domain, transaction?.skuId,
              transaction?.amount()
                  .toString(), iabInteract.getPreSelectedPaymentMethod(),
              transaction?.type, BillingAnalytics.RAKAM_PRESELECTED_PAYMENT_METHOD)
        } else {
          billingAnalytics.sendPurchaseStartWithoutDetailsEvent(transaction?.domain,
              transaction?.skuId, transaction?.amount()
              .toString(), transaction?.type,
              BillingAnalytics.RAKAM_PAYMENT_METHOD)
        }
        firstImpression = false
      }
    }
        .subscribeOn(networkScheduler)
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleAutoUpdate() {
    disposable.add(iabInteract.getAutoUpdateModel()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .filter {
          iabInteract.isHardUpdateRequired(it.blackList, it.updateVersionCode, it.updateMinSdk)
        }
        .doOnSuccess { view.showUpdateRequiredView() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleUserRegistration() {
    disposable.add(iabInteract.registerUser()
        .subscribeOn(networkScheduler)
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposable.clear()

  fun onSaveInstance(outState: Bundle) {
    outState.putBoolean(IabActivity.FIRST_IMPRESSION, firstImpression)
  }

  fun savePreselectedPaymentMethod(bundle: Bundle) {
    bundle.getString(PRE_SELECTED_PAYMENT_METHOD_KEY)
        ?.let {
          iabInteract.savePreSelectedPaymentMethod(it)
        }
  }
}