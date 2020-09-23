package com.asfoundation.wallet.billing.address

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class BillingAddressPresenter(
    private val view: BillingAddressView,
    private val disposables: CompositeDisposable,
    private val viewScheduler: Scheduler) {

  fun present() {
    handleSubmitClicks()
    handleBackClicks()
  }

  private fun handleSubmitClicks() {
    disposables.add(
        view.submitClicks()
            .subscribeOn(viewScheduler)
            .doOnNext { view.finishSuccess(it) }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleBackClicks() {
    disposables.add(
        view.backClicks()
            .subscribeOn(viewScheduler)
            .doOnNext { view.cancel() }
            .subscribe()
    )
  }

  fun stop() = disposables.clear()

}