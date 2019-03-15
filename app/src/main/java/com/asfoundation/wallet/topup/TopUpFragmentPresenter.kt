package com.asfoundation.wallet.topup

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.topup.TopUpActivity.Companion.APPC_C
import com.asfoundation.wallet.topup.TopUpActivity.Companion.LOCAL_CURRENCY
import com.asfoundation.wallet.topup.paymentMethods.PaymentMethodData
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit


class TopUpFragmentPresenter(private val view: TopUpFragmentView,
                             private val activity: TopUpActivityView?,
                             private val interactor: TopUpInteractor,
                             private val viewScheduler: Scheduler,
                             private val networkScheduler: Scheduler,
                             private val packageName: String) {

  companion object {
    const val DEFAULT_VALUE = "--"
    const val NETWORK_ID_ROPSTEN = 3L
    const val NETWORK_ID_MAIN = 1L
  }

  private lateinit var currentData: TopUpData
  private lateinit var paymentMethod: PaymentType
  private val disposables: CompositeDisposable = CompositeDisposable()
  private var currentCurrency = LOCAL_CURRENCY

  fun present() {
    setupUi()
    handleChangeCurrencyClick()
    handlePaymentMethodSelection()
    handleNextClick()
    handleValuesChange()
    handleAmountFocusChange()
  }

  private fun setupUi() {
    disposables.add(Single.zip(interactor.getPaymentMethods(), interactor.getLocalCurrency(),
        BiFunction { paymentMethods: List<PaymentMethodData>, currency: LocalCurrency ->
          TopUpData(paymentMethods,
              CurrencyData(currency.code, currency.symbol, DEFAULT_VALUE, "APPC-C", "APPC-C",
                  DEFAULT_VALUE))
        }).subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          currentData = it
          view.setupUiElements(currentData)
          handleAmountChange()
        }
        .subscribe())
  }

  private fun handleChangeCurrencyClick() {
    disposables.add(
        view.getChangeCurrencyClick().doOnNext {
          view.rotateChangeCurrencyButton()
          currentCurrency = if (currentCurrency == LOCAL_CURRENCY) APPC_C else LOCAL_CURRENCY
          updateValues()
        }.subscribe())
  }

  private fun handleNextClick() {
    disposables.add(
        view.getNextClick().doOnNext {
          view.showLoading()
          activity?.navigateToPayment(paymentMethod, currentData, currentCurrency, "BDS", "TOPUP")
        }.subscribe())
  }

  private fun handleAmountChange() {
    disposables.add(view.getEditTextChanges().debounce(800, TimeUnit.MILLISECONDS)
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

  private fun handleAmountFocusChange() {
    disposables.add(view.getEditTextFocusChanges().map {
      if (!it) {
        view.hideKeyboard()
      }
    }.subscribe())
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
    view.updateCurrencyData(currencyData)
  }


  private fun handlePaymentMethodSelection() {
    view.getPaymentMethodClick().map {
      paymentMethod = if (PaymentType.PAYPAL.subTypes.contains(it)) {
        PaymentType.PAYPAL
      } else {
        PaymentType.CARD
      }
    }.subscribe()
  }

  private fun handleValuesChange() {
    val amountChanged: Observable<Double> =
        view.getEditTextChanges().map {
          if (it.view().text.isNotEmpty() && it.view().text != DEFAULT_VALUE)
            it.view().text.toString().toDouble()
          else
            0.0
        }

    val paymentSelected: Observable<Boolean> = view.getPaymentMethodClick().map { it.isNotEmpty() }


    disposables.add(
        Observable.combineLatest(amountChanged, paymentSelected,
            BiFunction { amount: Double, isPaymentSelected: Boolean ->
              view.setNextButtonState(amount > 0 && isPaymentSelected)
            }).subscribe())
  }

//  private fun buildTransaction(amount: String, packageName: String): TransactionBuilder {
//    return TransactionBuilder("APPC", null, getNetworkId(),
//        null, BigDecimal(amount), null, 0, null,
//        "TOPUP", "BDS", packageName, null, null, null)
//        .shouldSendToken(true)
//  }

  private fun getNetworkId(): Long {
    return if (BuildConfig.DEBUG) NETWORK_ID_ROPSTEN else NETWORK_ID_MAIN
  }
}
