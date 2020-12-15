package com.asfoundation.wallet.billing.address

import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class BillingAddressPresenter(
    private val view: BillingAddressView,
    private val data: BillingAddressData,
    private val navigator: BillingAddressNavigator,
    private val billingAnalytics: BillingAnalytics,
    private val disposables: CompositeDisposable,
    private val viewScheduler: Scheduler) {

  fun present() {
    initializeView()
    handleSubmitClicks()
    handleBackClicks()
  }

  private fun initializeView() {
    view.initializeView(data.bonus, data.isDonation, data.domain, data.skuDescription,
        data.appcAmount, data.fiatAmount, data.fiatCurrency, data.isStored, data.shouldStoreCard)
  }

  private fun handleSubmitClicks() {
    disposables.add(
        view.submitClicks()
            .subscribeOn(viewScheduler)
            .doOnNext { billingAddressModel ->
              sendActionEventAnalytics(if (data.isDonation) "donate" else "buy")
              navigator.finishWithSuccess(billingAddressModel)
            }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleBackClicks() {
    disposables.add(
        view.backClicks()
            .subscribeOn(viewScheduler)
            .doOnNext {
              sendActionEventAnalytics("back")
              navigator.finishWithCancel()
            }
            .subscribe()
    )
  }

  private fun sendActionEventAnalytics(action: String) {
    billingAnalytics.sendBillingAddressActionEvent(data.domain, data.skuId,
        data.appcAmount.toString(), BillingAnalytics.PAYMENT_METHOD_CC,
        data.transactionType, action)
  }

  fun stop() = disposables.clear()

}