package com.asfoundation.wallet.topup

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import com.asf.wallet.R
import com.asfoundation.wallet.topup.TopUpActivity.Companion.WALLET_VALIDATION_REQUEST_CODE
import com.asfoundation.wallet.ui.iab.IabActivity
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class TopUpActivityPresenter(private val view: TopUpActivityView,
                             private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                             private val viewScheduler: Scheduler,
                             private val networkScheduler: Scheduler,
                             private val disposables: CompositeDisposable,
                             private val walletBlockedInteract: WalletBlockedInteract) {
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
    } else if (requestCode == WALLET_VALIDATION_REQUEST_CODE) {
      var errorMessage = data?.getIntExtra(IabActivity.ERROR_MESSAGE, 0)
      if (errorMessage == null || errorMessage == 0) {
        errorMessage = R.string.unknown_error
      }
      handleWalletBlockedCheck(errorMessage)
    }
  }

  private fun handleWalletBlockedCheck(@StringRes error: Int) {
    disposables.add(
        walletBlockedInteract.isWalletBlocked()
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              if (it) view.showError(error)
              else view.showTopUpScreen()
            }
            .subscribe({}, { handleError(it) })
    )
  }

  private fun handleError(throwable: Throwable) {
    throwable.printStackTrace()
    view.showError(R.string.unknown_error)
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
              view.finishActivity(bundle)
            }
            .doOnError { view.finish(bundle) }
            .subscribe({ }, { it.printStackTrace() })
    )
  }
}
