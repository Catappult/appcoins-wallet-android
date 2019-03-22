package com.asfoundation.wallet.topup

import com.asfoundation.wallet.topup.TopUpData.Companion.DEFAULT_VALUE
import com.asfoundation.wallet.topup.paymentMethods.PaymentMethodData
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


class TopUpFragmentPresenter(private val view: TopUpFragmentView,
                             private val activity: TopUpActivityView?,
                             private val interactor: TopUpInteractor,
                             private val viewScheduler: Scheduler,
                             private val networkScheduler: Scheduler) {

  private val disposables: CompositeDisposable = CompositeDisposable()

  fun present() {
    setupUi()
    handleChangeCurrencyClick()
    handleNextClick()
    handleValuesChange()
    handleAmountFocusChange()
    handleAmountChange()
  }

  fun stop() {
    disposables.dispose()
  }

  private fun setupUi() {
    disposables.add(Single.zip(
        interactor.getPaymentMethods().subscribeOn(networkScheduler).observeOn(viewScheduler),
        interactor.getLocalCurrency().subscribeOn(networkScheduler).observeOn(viewScheduler),
        BiFunction { paymentMethods: List<PaymentMethodData>, currency: LocalCurrency ->
          view.setupUiElements(filterPaymentMethods(paymentMethods), currency)
        }).subscribe())
  }

  private fun handleChangeCurrencyClick() {
    disposables.add(
        view.getChangeCurrencyClick().doOnNext {
          view.toggleSwitchCurrencyOn()
          view.rotateChangeCurrencyButton()
          view.switchCurrencyData()
          view.toggleSwitchCurrencyOff()
        }.subscribe())
  }

  private fun handleNextClick() {
    disposables.add(
        view.getNextClick().doOnNext {
          view.showLoading()
          activity?.navigateToPayment(it.paymentMethod!!, it, it.selectedCurrency, "BDS", "TOPUP")
        }.subscribe())
  }

  private fun handleAmountChange() {
    disposables.add(view.getEditTextChanges().filter { data: TopUpData ->
      (data.selectedCurrency == TopUpData.FIAT_CURRENCY && data.currency.fiatValue != DEFAULT_VALUE)
          || (data.selectedCurrency == TopUpData.APPC_C_CURRENCY && data.currency.appcValue != DEFAULT_VALUE)
    }.debounce(700, TimeUnit.MILLISECONDS)
        .switchMap { topUpData ->
          if (topUpData.selectedCurrency == TopUpData.FIAT_CURRENCY) {
            interactor.convertLocal(topUpData.currency.fiatCurrencyCode,
                topUpData.currency.fiatValue)
          } else {
            interactor.convertAppc(topUpData.currency.appcValue)
          }.subscribeOn(networkScheduler).map { value ->
            if (topUpData.selectedCurrency == TopUpData.FIAT_CURRENCY) {
              topUpData.currency.appcValue = value.amount.toString()
            } else {
              topUpData.currency.fiatValue = value.amount.toString()
            }
          }.observeOn(viewScheduler).map {
            view.setConversionValue(topUpData)
          }
        }
        .subscribe())
  }

  private fun handleAmountFocusChange() {
    disposables.add(view.getEditTextFocusChanges().map {
      if (!it) {
        view.hideKeyboard()
      }
    }.subscribe())
  }

  private fun handleValuesChange() {
    disposables.add(view.getEditTextChanges().map {
      it.currency.fiatValue.isNotEmpty()
          && it.currency.fiatValue != DEFAULT_VALUE
          && BigDecimal(it.currency.fiatValue) > BigDecimal.ZERO
          && it.paymentMethod != null
    }.map { view.setNextButtonState(it) }.subscribe())

  }

  private fun filterPaymentMethods(methods: List<PaymentMethodData>): List<PaymentMethodData> {
    return methods.filter { it.id == "paypal" || it.id == "credit_card" }
  }
}
