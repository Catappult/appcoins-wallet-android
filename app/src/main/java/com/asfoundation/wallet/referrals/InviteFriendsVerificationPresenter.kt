package com.asfoundation.wallet.referrals

import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class InviteFriendsVerificationPresenter(private val view: InviteFriendsVerificationView,
                                         private val disposable: CompositeDisposable) {

  fun present() {
    handleVerifyClick()
    handleBeenInvitedClick()
  }

  private fun handleVerifyClick() {
    disposable.add(view.verifyButtonClick()
        .throttleFirst(1, TimeUnit.SECONDS)
        .doOnNext { view.navigateToWalletValidation(false) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleBeenInvitedClick() {
    disposable.add(view.beenInvitedClick()
        .throttleFirst(1, TimeUnit.SECONDS)
        .doOnNext { view.navigateToWalletValidation(true) }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposable.clear()
  }

}