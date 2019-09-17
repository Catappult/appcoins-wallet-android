package com.asfoundation.wallet.referrals

import io.reactivex.disposables.CompositeDisposable

class InviteFriendsVerificationPresenter(private val view: InviteFriendsVerificationView,
                                         private val disposable: CompositeDisposable) {

  fun present() {
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

  fun stop() {
    disposable.clear()
  }

}