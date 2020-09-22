package com.asfoundation.wallet.billing.address

import com.asfoundation.wallet.logging.Logger
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class BillingAddressPresenter(
    private val view: BillingAddressView,
    private val disposables: CompositeDisposable,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler,
    private val billingAddressInteractor: BillingAddressInteractor,
    private val billingPaymentModel: BillingPaymentModel,
    private val logger: Logger) {

  companion object {
    private val TAG = BillingAddressPresenter::class.java.name
  }

  fun present() {
    handleSubmitClicks()
    handleBackClicks()
  }

  private fun handleSubmitClicks() {
    disposables.add(
        view.submitClicks()
            .subscribeOn(viewScheduler)
            .doOnNext { view.showLoading() }
            .observeOn(networkScheduler)
            .flatMapSingle { billingAddressInteractor.makePayment(billingPaymentModel, it) }
            .subscribe({}, {
              logger.log(TAG, it)
            })
    )
  }

  private fun handleBackClicks() {
    disposables.add(
        view.backClicks()
            .subscribeOn(viewScheduler)
            .doOnNext { view.showMoreMethods() }
            .subscribe()
    )
  }

  fun stop() = disposables.clear()

}