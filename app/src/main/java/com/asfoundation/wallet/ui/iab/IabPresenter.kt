package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.interact.AutoUpdateInteract
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by franciscocalado on 20/07/2018.
 */

class IabPresenter(private val view: IabView,
                   private val autoUpdateInteract: AutoUpdateInteract,
                   private val networkScheduler: Scheduler,
                   private val viewScheduler: Scheduler,
                   private val disposable: CompositeDisposable,
                   private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                   private val billingAnalytics: BillingAnalytics,
                   private var firstImpression: Boolean) {

  fun present() {
    handleAutoUpdate()
  }

  fun handleBackupNotifications(bundle: Bundle) {
    disposable.add(
        inAppPurchaseInteractor.incrementAndValidateNotificationNeeded()
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess { notificationNeeded ->
              if (notificationNeeded.isNeeded)
                view.showBackupNotification(notificationNeeded.walletAddress)
              view.finishAfterNotification(bundle)
            }
            .doOnError { view.finish(bundle) }
            .subscribe({ }, { it.printStackTrace() })
    )
  }

  fun handlePurchaseStartAnalytics(transaction: TransactionBuilder?) {
    disposable.add(Completable.fromAction {
      if (firstImpression) {
        if (inAppPurchaseInteractor.hasPreSelectedPaymentMethod()) {
          billingAnalytics.sendPurchaseStartEvent(transaction?.domain, transaction?.skuId,
              transaction?.amount()
                  .toString(), inAppPurchaseInteractor.preSelectedPaymentMethod,
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
    disposable.add(autoUpdateInteract.getAutoUpdateModel()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .filter {
          autoUpdateInteract.isHardUpdateRequired(it.blackList,
              it.updateVersionCode, it.updateMinSdk)
        }
        .doOnSuccess { view.showUpdateRequiredView() }
        .subscribe())
  }

  fun stop() {
    disposable.clear()
  }

  fun onSaveInstance(outState: Bundle) {
    outState.putBoolean(IabActivity.FIRST_IMPRESSION, firstImpression)
  }
}