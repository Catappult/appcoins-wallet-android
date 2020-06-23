package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.IabInteract.Companion.PRE_SELECTED_PAYMENT_METHOD_KEY
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by franciscocalado on 20/07/2018.
 */

class IabPresenter(private val view: IabView,
                   private val networkScheduler: Scheduler,
                   private val viewScheduler: Scheduler,
                   private val disposable: CompositeDisposable,
                   private val billingAnalytics: BillingAnalytics,
                   private var firstImpression: Boolean,
                   private val iabInteract: IabInteract) {

  fun present() {
    handleAutoUpdate()
    handleUserRegistration()
  }

  fun handleBackupNotifications(bundle: Bundle) {
    disposable.add(
        iabInteract.incrementAndValidateNotificationNeeded()
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
        .subscribe())
  }

  private fun handleUserRegistration() {
    disposable.add(iabInteract.registerUser()
        .subscribeOn(networkScheduler)
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposable.clear()
  }

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