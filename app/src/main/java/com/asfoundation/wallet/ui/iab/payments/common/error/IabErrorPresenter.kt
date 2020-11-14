package com.asfoundation.wallet.ui.iab.payments.common.error

import io.reactivex.disposables.CompositeDisposable

class IabErrorPresenter(
    private val view: IabErrorView,
    private val data: IabErrorData,
    private val navigator: IabErrorNavigator,
    private val disposables: CompositeDisposable) {

  fun present() {
    initializeView()
    handleBackClick()
    handleCancelClick()
    handleOtherPaymentsClick()
  }

  private fun initializeView() {
    view.setErrorMessage(data.errorMessage)
    view.setSupportVisibility(data.showSupport)
  }

  private fun handleOtherPaymentsClick() {
    disposables.add(
        view.otherPaymentMethodsClickEvent()
            .doOnNext { navigator.navigateToOtherPayments(data.backStackEntryName) }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleCancelClick() {
    disposables.add(
        view.cancelClickEvent()
            .doOnNext { navigator.cancelPayment() }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleBackClick() {
    disposables.add(
        view.backClickEvent()
            .doOnNext { navigator.navigateBackToPayment(data.backStackEntryName) }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()
}