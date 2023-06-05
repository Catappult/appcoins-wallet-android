package com.asfoundation.wallet.topup.address

import com.asfoundation.wallet.billing.address.BillingAddressModel
import com.asfoundation.wallet.billing.address.BillingAddressRepository
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.asfoundation.wallet.topup.TopUpAnalytics
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class BillingAddressTopUpPresenter(private val view: BillingAddressTopUpView,
                                   private val data: BillingAddressTopUpData,
                                   private val disposables: CompositeDisposable,
                                   private val viewScheduler: Scheduler,
                                   private val navigator: BillingAddressTopUpNavigator,
                                   private val billingAddressRepository: BillingAddressRepository,
                                   private val topUpAnalytics: TopUpAnalytics) {

  fun present() {
    initializeView()
    handleSubmitClicks()
  }

  private fun initializeView() {
    view.initializeView(data.data, data.fiatAmount, data.fiatCurrency, data.shouldStoreCard,
        data.isStored, billingAddressRepository.retrieveBillingAddress())
  }

  private fun handleSubmitClicks() {
    disposables.add(
        view.submitClicks()
            .subscribeOn(viewScheduler)
            .doOnNext { billingAddressModel ->
              val billingModel =
                  BillingAddressModel(billingAddressModel.address, billingAddressModel.city,
                      billingAddressModel.zipcode, billingAddressModel.state,
                      billingAddressModel.country, billingAddressModel.number, data.shouldStoreCard)
              if (data.shouldStoreCard) {
                billingAddressRepository.saveBillingAddress(billingModel)
              }
              topUpAnalytics.sendBillingAddressActionEvent(data.data.appcValue.toDouble(),
                  BillingAnalytics.PAYMENT_METHOD_CC, "top up")
              view.finishSuccess(billingModel)
              navigator.navigateBack()
            }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()
}