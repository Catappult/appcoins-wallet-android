package com.asfoundation.wallet.topup.address

import com.asfoundation.wallet.billing.address.BillingAddressInteractor
import com.asfoundation.wallet.logging.Logger
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class BillingAddressTopUpPresenter(
    private val view: BillingAddressTopUpView,
    private val disposables: CompositeDisposable,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler,
    private val billingAddressInteractor: BillingAddressInteractor,
    private val billingPaymentTopUpModel: BillingPaymentTopUpModel,
    private val logger: Logger) {

  companion object {
    private val TAG = BillingAddressTopUpPresenter::class.java.name
  }

  fun present() {
    handleSubmitClicks()
  }

  private fun handleSubmitClicks() {
    disposables.add(
        view.submitClicks()
            .subscribeOn(viewScheduler)
            .doOnNext { view.showLoading() }
            .observeOn(networkScheduler)
            .flatMapSingle {
              billingAddressInteractor.makeTopUpPayment(billingPaymentTopUpModel, it)
            }
            .subscribe({}, {
              logger.log(TAG, it)
            })
    )
  }

  fun stop() = disposables.clear()

}