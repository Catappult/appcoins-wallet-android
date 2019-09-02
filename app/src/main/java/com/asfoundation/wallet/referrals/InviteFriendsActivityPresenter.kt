package com.asfoundation.wallet.referrals

import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.io.IOException
import java.util.concurrent.TimeUnit

class InviteFriendsActivityPresenter(private val activity: InviteFriendsActivityView,
                                     private val smsValidationInteract: SmsValidationInteract,
                                     private val walletInteract: FindDefaultWalletInteract,
                                     private val disposables: CompositeDisposable,
                                     private val networkScheduler: Scheduler,
                                     private val viewScheduler: Scheduler) {

  fun present() {
    handleFragmentNavigation()
    handleRetryClick()
  }

  private fun handleFragmentNavigation() {
    disposables.add(walletInteract.find()
        .flatMap { smsValidationInteract.isValid(it) }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { handleValidationResult(it) }
        .subscribe({}, { handlerError(it) })
    )
  }

  private fun handleValidationResult(validationStatus: WalletValidationStatus) {
    when (validationStatus) {
      WalletValidationStatus.SUCCESS -> {
        activity.navigateToInviteFriends()
        handleInfoButtonVisibility()
      }
      WalletValidationStatus.NO_NETWORK -> activity.showNetworkErrorView()
      else -> activity.navigateToVerificationFragment()
    }
  }

  private fun handleInfoButtonVisibility() {
    disposables.add(activity.infoButtonInitialized()
        .filter { it }
        .doOnNext { activity.showInfoButton() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handlerError(throwable: Throwable) {
    throwable.printStackTrace()
    if (isNoNetworkException(throwable)) {
      activity.showNetworkErrorView()
    }
  }

  private fun isNoNetworkException(throwable: Throwable): Boolean {
    return throwable is IOException || throwable.cause != null && throwable.cause is IOException
  }

  private fun handleRetryClick() {
    disposables.add(activity.retryClick()
        .observeOn(viewScheduler)
        .doOnNext { activity.showRetryAnimation() }
        .delay(1, TimeUnit.SECONDS)
        .doOnNext { handleFragmentNavigation() }
        .subscribe({}, { it.printStackTrace() }))
  }


  fun stop() {
    disposables.clear()
  }
}