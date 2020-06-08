package com.asfoundation.wallet.topup

import android.content.Intent
import android.os.Bundle
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.WebViewActivity
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class TopUpActivityPresenter(private val view: TopUpActivityView,
                             private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                             private val viewScheduler: Scheduler,
                             private val networkScheduler: Scheduler,
                             private val disposables: CompositeDisposable) {
  fun present(isCreating: Boolean) {
    if (isCreating) {
      view.showTopUpScreen()
    }
  }

  fun processActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == TopUpActivity.WEB_VIEW_REQUEST_CODE) {
      if (resultCode == WebViewActivity.SUCCESS && data != null) {
        data.data?.let { view.acceptResult(it) } ?: view.cancelPayment()
      } else if (resultCode == WebViewActivity.FAIL) {
        view.cancelPayment()
      }
    }
  }

  fun handleBackupNotifications(bundle: Bundle) {
    disposables.add(
        inAppPurchaseInteractor.incrementAndValidateNotificationNeeded()
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess { notificationNeeded ->
              if (notificationNeeded.isNeeded) {
                view.showBackupNotification(notificationNeeded.walletAddress)
              }
              view.finishAfterNotification(bundle)
            }
            .doOnError { view.finish(bundle) }
            .subscribe({ }, { it.printStackTrace() })
    )
  }
}
