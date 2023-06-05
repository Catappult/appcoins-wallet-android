package com.asfoundation.wallet.billing.address

import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class BillingAddressPresenter(
    private val view: BillingAddressView,
    private val data: BillingAddressData,
    private val navigator: BillingAddressNavigator,
    private val billingAddressRepository: BillingAddressRepository,
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
        data.appcAmount, data.fiatAmount, data.fiatCurrency, data.isStored, data.shouldStoreCard,
        billingAddressRepository.retrieveBillingAddress())
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
              sendActionEventAnalytics(if (data.isDonation) "donate" else "buy")
              navigator.finishWithSuccess(billingModel)
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