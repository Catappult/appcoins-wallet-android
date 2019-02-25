package com.asfoundation.wallet.topup

import com.asfoundation.wallet.topup.paymentMethods.PaymentMethodData
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

class TopUpFragmentPresenter(private val view: TopUpFragmentView,
                             private val interactor: TopUpInteractor,
                             private val viewScheduler: Scheduler,
                             private val networkScheduler: Scheduler) {

  companion object {
    val LOCAL_CURRENCY = "LOCAL_CURRENCY"
    val APPC_C = "APPC_C"
  }

  private val disposables: CompositeDisposable = CompositeDisposable()
  private lateinit var currentData: UiData

  private var currentCurrency = LOCAL_CURRENCY

  fun present() {
    setupUi()

    handleChangeCurrencyClick()
  }

  private fun setupUi() {
    disposables.add(Single.zip(interactor.getPaymentMethods(), interactor.getLocalCurrency(),
        BiFunction { paymentMethods: List<PaymentMethodData>, currency: LocalCurrency ->
          UiData(paymentMethods,
              CurrencyData(currency.code, currency.symbol, "0", "APPC-C", "APPC-C", "--"))
        }).subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          currentData = it
          view.setupUiElements(currentData)
          handleValueChange()
        }
        .subscribe())
  }

  private fun handleChangeCurrencyClick() {
    disposables.add(
        view.getChangeCurrencyClick().doOnNext {
          currentCurrency = if (currentCurrency == LOCAL_CURRENCY) APPC_C else LOCAL_CURRENCY
          updateValues()
        }.subscribe())
  }

  private fun handleValueChange() {
    disposables.add(view.getEditTextChanges().debounce(500, TimeUnit.MILLISECONDS)
        .map { it.view().text.toString() }
        .filter { isValueChanged(it) }.switchMap {
          if (currentCurrency == LOCAL_CURRENCY) {
            currentData.currency.fiatValue = it
            interactor.convertLocal(currentData.currency.fiatCurrencyCode, it)
          } else {
            currentData.currency.appcValue = it
            interactor.convertAppc(it)
          }
        }
        .subscribeOn(networkScheduler).observeOn(
            viewScheduler).subscribe { value ->
          if (currentCurrency == LOCAL_CURRENCY) {
            currentData.currency.appcValue = value.amount.toString()
          } else {
            currentData.currency.fiatValue = value.amount.toString()
          }
          updateValues()
        })
  }

  private fun isValueChanged(value: String): Boolean {
    val currentValue =
        if (currentCurrency == LOCAL_CURRENCY) currentData.currency.fiatValue else currentData.currency.appcValue
    return value.isNotEmpty() && currentValue != value
  }

  private fun updateValues() {
    var currencyData = currentData.currency
    if (currentCurrency == APPC_C) {
      currencyData = CurrencyData(currencyData.appcCode,
          currencyData.appcSymbol, currencyData.appcValue,
          currencyData.fiatCurrencyCode, currencyData.fiatCurrencySymbol,
          currencyData.fiatValue)
    }
    view.changeCurrency(currencyData)

  }
}
