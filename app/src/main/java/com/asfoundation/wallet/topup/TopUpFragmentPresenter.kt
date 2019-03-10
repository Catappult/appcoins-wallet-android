package com.asfoundation.wallet.topup

import com.adyen.core.models.PaymentMethod
import com.asfoundation.wallet.billing.adyen.Adyen
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.topup.paymentMethods.PaymentMethodData
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.io.IOException
import java.util.concurrent.TimeUnit


class TopUpFragmentPresenter(private val view: TopUpFragmentView,
                             private val activity: TopUpActivityView?,
                             private val interactor: TopUpInteractor,
                             private val paymentHandler: Adyen,
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
    handleNextClick()
    handleValuesChange()
    handleAmountFocusChange()
    handlePaymentDetailsRequired()
  }

  private fun setupUi() {
    disposables.add(Single.zip(interactor.getPaymentMethods(), interactor.getLocalCurrency(),
        BiFunction { paymentMethods: List<PaymentMethodData>, currency: LocalCurrency ->
          UiData(paymentMethods,
              CurrencyData(currency.code, currency.symbol, "", "APPC-C", "APPC-C", "--"))
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
          currentCurrency = if (currentCurrency == LOCAL_CURRENCY) APPC_C else LOCAL_CURRENCY
          updateValues()
        }.subscribe())
  }

  private fun handleNextClick() {
    disposables.add(
        view.getNextClick().doOnNext {
          view.showLoading()
          activity?.navigateToAdyen(true, "EUR", PaymentType.CARD)
//          paymentHandler.createNewPayment()
//          selectPaymentMethod(PaymentType.CARD)
        }.subscribe())
  }

  private fun handleAmountChange() {
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

  private fun handleValuesChange() {
    val amountChanged: Observable<Double> =
        view.getEditTextChanges().map {
          if (it.view().text.isNotEmpty())
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

  private fun handlePaymentDetailsRequired() {
    disposables.add(paymentHandler.paymentRequest
        .filter { paymentRequest -> paymentRequest.paymentMethod != null }
        .map { paymentRequest ->
          paymentRequest.paymentMethod!!.type
        }
        .distinctUntilChanged { paymentRequest, paymentRequest2 -> paymentRequest == paymentRequest2 }
        .flatMapMaybe {
          paymentHandler.paymentRequest.firstElement()
        }
        .observeOn(viewScheduler)
        .doOnNext { data ->
          if (data.paymentMethod!!.type == PaymentMethod.Type.CARD) {
            view.showPaymentDetailsForm()
            // data.getPaymentMethod(), data.getAmount(), true,
            //            data.getShopperReference() != null, data.getPublicKey(), data.getGenerationTime())
          } else {
            view.showPaymentDetailsForm()
            //showCvcView(data.getAmount(), data.getPaymentMethod())
          }
        }
        .observeOn(viewScheduler)
        .subscribe({ }, { throwable -> showError(throwable) }))
  }

  private fun selectPaymentMethod(paymentType: PaymentType) {
    disposables.add(paymentHandler.getPaymentMethod(paymentType)
        .flatMapCompletable {
          paymentMethod -> paymentHandler.selectPaymentService(paymentMethod)
        }.observeOn(viewScheduler)
        .subscribe({ }, { throwable -> showError(throwable) }))
  }

  private fun showError(throwable: Throwable) {
    throwable.printStackTrace()

    if (throwable is IOException) {
//      view.hideLoading()
//      view.showNetworkError()
    } else {
//      view.showGenericError()
    }
  }
}
