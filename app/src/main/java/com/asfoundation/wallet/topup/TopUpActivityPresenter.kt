package com.asfoundation.wallet.topup

import android.content.Intent
import android.os.Bundle
import com.appcoins.wallet.commons.Logger
import com.asf.wallet.R
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.promotions.usecases.StartVipReferralPollingUseCase
import com.asfoundation.wallet.ui.iab.BillingWebViewFragment
import com.asfoundation.wallet.ui.iab.WebViewActivity
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class TopUpActivityPresenter(
  private val view: TopUpActivityView,
  private val topUpInteractor: TopUpInteractor,
  private val startVipReferralPollingUseCase: StartVipReferralPollingUseCase,
  private val viewScheduler: Scheduler,
  private val networkScheduler: Scheduler,
  private val disposables: CompositeDisposable,
  private val logger: Logger
) {
  fun present(isCreating: Boolean) {
    if (isCreating) {
      view.showTopUpScreen()
    }
    handleSupportClicks()
    handleTryAgainClicks()
  }

  private fun handleSupportClicks() {
    disposables.add(view.getSupportClicks()
      .throttleFirst(50, TimeUnit.MILLISECONDS)
      .observeOn(viewScheduler)
      .flatMapCompletable { topUpInteractor.showSupport() }
      .subscribe({}, { handleError(it) })
    )
  }

  private fun handleTryAgainClicks() {
    disposables.add(view.getTryAgainClicks()
      .throttleFirst(50, TimeUnit.MILLISECONDS)
      .observeOn(viewScheduler)
      .doOnNext { view.showTopUpScreen() }
      .subscribe({}, { handleError(it) })
    )
  }

  fun processActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == TopUpActivity.WEB_VIEW_REQUEST_CODE) {
      when (resultCode) {
        WebViewActivity.FAIL -> {
          if (data?.dataString?.contains(BillingWebViewFragment.OPEN_SUPPORT) == true) {
            logger.log(TAG, Exception("ActivityResult ${data.dataString}"))
            cancelPaymentAndShowSupport()
          } else {
            view.cancelPayment()
          }
        }
        WebViewActivity.SUCCESS -> {
          data?.data?.let { view.acceptResult(it) } ?: view.cancelPayment()
        }
        WebViewActivity.USER_CANCEL -> {
          view.cancelPayment()
        }
      }
    }
  }

  private fun cancelPaymentAndShowSupport() {
    disposables.add(topUpInteractor.showSupport()
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .doOnComplete { view.cancelPayment() }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleError(throwable: Throwable) {
    logger.log(TAG, throwable)
    view.showError(R.string.unknown_error)
  }

  fun handlePerkNotifications(bundle: Bundle) {
    disposables.add(topUpInteractor.getWalletAddress()
      .subscribeOn(networkScheduler)
      .flatMap { startVipReferralPollingUseCase(Wallet(it)).toSingleDefault(it) }
      .observeOn(viewScheduler)
      .doOnSuccess {
        view.launchPerkBonusAndGamificationService(it)
        view.finishActivity(bundle)
      }
      .doOnError { view.finishActivity(bundle) }
      .subscribe({}, { it.printStackTrace() })
    )
  }


  fun handleBackupNotifications(bundle: Bundle) {
    disposables.add(topUpInteractor.incrementAndValidateNotificationNeeded()
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

  companion object {
    private val TAG = TopUpActivityPresenter::class.java.name
  }
}
