package com.asfoundation.wallet.ui

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class AuthenticationErrorBottomSheetPresenter(private val view: AuthenticationErrorBottomSheetView,
                                              private val viewScheduler: Scheduler,
                                              private val disposables: CompositeDisposable) {

  fun present() {
    view.setMessage()
    view.setupUi()
    handleButtonClick()
  }

  private fun handleButtonClick() {
    disposables.add(view.getButtonClick()
        .observeOn(viewScheduler)
        .doOnNext { view.retryAuthentication() }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.clear()
}
