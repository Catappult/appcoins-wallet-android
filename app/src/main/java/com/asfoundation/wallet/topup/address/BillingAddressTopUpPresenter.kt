package com.asfoundation.wallet.topup.address

import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.topup.TopUpAnalytics
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class BillingAddressTopUpPresenter(private val view: BillingAddressTopUpView,
                                   private val data: BillingAddressTopUpData,
                                   private val disposables: CompositeDisposable,
                                   private val viewScheduler: Scheduler,
                                   private val navigator: BillingAddressTopUpNavigator,
                                   private val topUpAnalytics: TopUpAnalytics) {

  fun present() {
    initializeView()
    handleSubmitClicks()
  }

  private fun initializeView() {
    view.initializeView(data.data, data.fiatAmount, data.fiatCurrency, data.shouldStoreCard,
        data.isStored)
  }

  private fun handleSubmitClicks() {
    disposables.add(
        view.submitClicks()
            .subscribeOn(viewScheduler)
            .doOnNext {
              topUpAnalytics.sendBillingAddressActionEvent(data.data.appcValue.toDouble(),
                  BillingAnalytics.PAYMENT_METHOD_CC, "top up")
              view.finishSuccess(it)
              navigator.navigateBack()
            }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()
}