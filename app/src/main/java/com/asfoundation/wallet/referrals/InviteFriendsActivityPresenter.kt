package com.asfoundation.wallet.referrals

import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.io.IOException
import java.util.concurrent.TimeUnit

class InviteFriendsActivityPresenter(private val activity: InviteFriendsActivityView,
                                     private val referralInteractor: ReferralInteractorContract,
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
        .flatMap { referralInteractor.retrieveReferral() }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { handleValidationResult(it) }
        .flatMapCompletable {
          referralInteractor.saveReferralInformation(it.completed, it.receivedAmount.toString(),
              it.link != null, ReferralsScreen.INVITE_FRIENDS)
        }
        .subscribe({}, { handlerError(it) })
    )
  }

  private fun handleValidationResult(referral: ReferralResponse) {
    if (referral.link != null) {
      activity.navigateToInviteFriends(referral.amount, referral.pendingAmount,
          referral.currency, referral.link, referral.completed, referral.receivedAmount,
          referral.maxAmount, referral.available)
      handleInfoButtonVisibility()
    } else {
      activity.navigateToVerificationFragment(referral.amount, referral.currency)
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