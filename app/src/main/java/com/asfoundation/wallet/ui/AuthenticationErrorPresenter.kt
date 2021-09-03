package com.asfoundation.wallet.ui

import io.reactivex.disposables.CompositeDisposable

class AuthenticationErrorPresenter(
    private val view: AuthenticationErrorView,
    private val activityView: AuthenticationPromptView,
    private val disposables: CompositeDisposable) {

  fun present() {
    handleOutsideOfBottomSheetClick()
  }

  private fun handleOutsideOfBottomSheetClick() {
    disposables.add(view.outsideOfBottomSheetClick()
        .doOnNext { activityView.closeCancel() }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.clear()
}
