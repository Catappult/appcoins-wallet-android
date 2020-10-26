package com.asfoundation.wallet.topup.address

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class BillingAddressTopUpPresenter(
    private val view: BillingAddressTopUpView,
    private val disposables: CompositeDisposable,
    private val viewScheduler: Scheduler) {

  fun present() {
    handleSubmitClicks()
  }

  private fun handleSubmitClicks() {
    disposables.add(
        view.submitClicks()
            .subscribeOn(viewScheduler)
            .doOnNext { view.finishSuccess(it) }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()

}