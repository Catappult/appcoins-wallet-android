package com.asfoundation.wallet.billing.address

import android.os.Bundle
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class BillingAddressPresenter(private val view: BillingAddressView,
                              private val disposables: CompositeDisposable,
                              private val viewScheduler: Scheduler,
                              private val networkScheduler: Scheduler,
                              private val billingAddressInteractor: BillingAddressInteractor) {

  fun present() {
    handleSubmitClicks()
    handleBackClicks()
  }

  private fun handleSubmitClicks() {
    disposables.add(
        view.submitClicks()
            .subscribeOn(viewScheduler)
            .observeOn(networkScheduler)
            .flatMapSingle { billingAddressInteractor.makePayment() }
            .subscribe()
    )
  }

  private fun handleBackClicks() {
    disposables.add(
        view.backClicks()
            .subscribe()
    )
  }

  fun onSaveInstanceState(outState: Bundle) {

  }

  fun stop() = disposables.clear()

}