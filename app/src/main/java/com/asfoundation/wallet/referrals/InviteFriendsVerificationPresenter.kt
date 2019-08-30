package com.asfoundation.wallet.referrals

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class InviteFriendsVerificationPresenter(private val view: InviteFriendsVerificationView,
                                         private val referralInteractor: ReferralInteractorContract,
                                         private val disposable: CompositeDisposable,
                                         private val viewScheduler: Scheduler,
                                         private val networkScheduler: Scheduler) {

  fun present() {
    retrieveReferral()
    handleVerifyClick()
    handleBeenInvitedClick()
  }

  private fun handleVerifyClick() {
    disposable.add(view.verifyButtonClick()
        .doOnNext { view.navigateToWalletValidation(false) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleBeenInvitedClick() {
    disposable.add(view.beenInvitedClick()
        .doOnNext { view.navigateToWalletValidation(true) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun retrieveReferral() {
    disposable.add(referralInteractor.retrieveReferral()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.setDescriptionText(it.amount, it.currency) }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposable.clear()
  }

}