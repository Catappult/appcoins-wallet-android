package com.asfoundation.wallet.topup

import android.util.Log
import com.appcoins.wallet.gamification.repository.ForecastBonus
import com.asfoundation.wallet.topup.TopUpData.Companion.DEFAULT_VALUE
import com.asfoundation.wallet.topup.paymentMethods.PaymentMethodData
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Observable
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
  private var gamificationLevel = 0
  private var hasDefaultValues = false

  companion object {
    private const val NUMERIC_REGEX = "^([1-9]|[0-9]+[,.]+[0-9])[0-9]*?\$"
  }

  fun present(appPackage: String) {
    setupUi()
    handleChangeCurrencyClick()
    handleNextClick()
    handleRetryClick()
    handleManualAmountChange(appPackage)
    handlePaymentMethodSelected()
    handleValuesClicks()
    handleValues()
    handleKeyboardEvents()
  }

  fun stop() {
    interactor.cleanCachedValues()
    disposables.dispose()
  }

  private fun setupUi() {
    disposables.add(Single.zip(
            interactor.getPaymentMethods()
                .subscribeOn(networkScheduler)
                .observeOn(viewScheduler),
            interactor.getLimitTopUpValues()
                .subscribeOn(networkScheduler)
                .observeOn(viewScheduler),
            BiFunction { paymentMethods: List<PaymentMethodData>, values: TopUpLimitValues ->
              view.setupUiElements(filterPaymentMethods(paymentMethods),
                  LocalCurrency(values.maxValue.symbol, values.maxValue.currency))
            })
        .subscribe({}, { handleError(it) }))
  }

  private fun handleValues() {
    disposables.add(interactor.getDefaultValues()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          if (it.error.hasError || it.values.size < 3) {
            view.hideValuesAdapter()
            hasDefaultValues = false
          } else {
            view.setValuesAdapter(it.values)
            hasDefaultValues = true
          }
        }
        .subscribe())
  }

  private fun handleKeyboardEvents() {
    disposables.add(
        view.getKeyboardEvents()
            .doOnNext {
              if (it && hasDefaultValues) view.showValuesAdapter()
              else view.hideValuesAdapter()
            }
            .subscribeOn(viewScheduler)
            .subscribe()
    )
  }

  private fun handleError(throwable: Throwable) {
    if (throwable.isNoNetworkException()) view.showNoNetworkError()
  }

  private fun handleChangeCurrencyClick() {
    disposables.add(view.getChangeCurrencyClick()
        .doOnNext {
          view.toggleSwitchCurrencyOn()
          view.rotateChangeCurrencyButton()
          view.switchCurrencyData()
          view.toggleSwitchCurrencyOff()
        }
        .subscribe())
  }

  private fun handleNextClick() {
    disposables.add(
        view.getNextClick()
            .filter {
              val limitValues = interactor.getLimitTopUpValues()
                  .blockingGet()
              isCurrencyValid(it.currency) && isValueInRange(limitValues,
                  it.currency.fiatValue.toDouble())
            }
            .doOnNext {
              view.showLoading()
              showPaymentDetails(it)
            }
            .subscribe())
  }

  private fun showPaymentDetails(topUpData: TopUpData) {
    disposables.add(interactor.getDefaultValues()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          activity?.navigateToPayment(topUpData.paymentMethod!!, topUpData,
              topUpData.selectedCurrency, "TOPUP", topUpData.bonusValue, it.values,
              gamificationLevel)
          view.hideLoading()
        }
        .subscribe())
  }

  private fun handleManualAmountChange(packageName: String) {
    disposables.add(view.getEditTextChanges()
        .doOnNext { resetValues(it) }
        .debounce(700, TimeUnit.MILLISECONDS, viewScheduler)
        .doOnNext { handleInputValue(it) }
        .filter { isNumericOrEmpty(it) }
        .switchMap { topUpData ->
          getConvertedValue(topUpData)
              .subscribeOn(networkScheduler)
              .map { value -> updateConversionValue(value.amount, topUpData) }
              .observeOn(viewScheduler)
              .filter { isConvertedValueAvailable(it) }
              .doOnComplete { view.setConversionValue(topUpData) }
              .flatMap {
                interactor.getLimitTopUpValues()
                    .toObservable()
                    .flatMap { handleInsertedValue(packageName, topUpData, it) }
              }
              .doOnError { it.printStackTrace() }
              .onErrorResumeNext(Observable.empty())
        }
        .subscribe())
  }

  private fun handleInvalidFormatInput() {
    handleEmptyOrDefaultInput()
    view.hideValueInputWarning()
    view.changeMainValueColor(false)
  }

  private fun handleEmptyOrDefaultInput() {
    view.hideBonus()
    view.setNextButtonState(false)
  }

  private fun resetValues(topUpData: TopUpData) {
    view.setNextButtonState(false)
    view.hideValueInputWarning()
    updateConversionValue(BigDecimal.ZERO, topUpData)
    view.setConversionValue(topUpData)
  }

  private fun handleInputValue(topUpData: TopUpData) {
    if (isNumericOrEmpty(topUpData)) {
      if (topUpData.currency.fiatValue == DEFAULT_VALUE) {
        handleEmptyOrDefaultInput()
      }
    } else {
      handleInvalidFormatInput()
    }
  }

  private fun isNumericOrEmpty(data: TopUpData): Boolean {
    return if (data.selectedCurrency == TopUpData.FIAT_CURRENCY) {
      data.currency.fiatValue == DEFAULT_VALUE || data.currency.fiatValue.matches(
          NUMERIC_REGEX.toRegex())
    } else {
      data.currency.appcValue == DEFAULT_VALUE || data.currency.appcValue.matches(
          NUMERIC_REGEX.toRegex())
    }
  }

  private fun filterPaymentMethods(methods: List<PaymentMethodData>): List<PaymentMethodData> {
    return methods.filter { it.id == "paypal" || it.id == "credit_card" }
  }

  private fun getConvertedValue(data: TopUpData): Observable<FiatValue> {
    return if (data.selectedCurrency == TopUpData.FIAT_CURRENCY
        && data.currency.fiatValue != DEFAULT_VALUE) {
      interactor.convertLocal(data.currency.fiatCurrencyCode,
          data.currency.fiatValue, 2)
    } else if (data.selectedCurrency == TopUpData.APPC_C_CURRENCY
        && data.currency.appcValue != DEFAULT_VALUE) {
      interactor.convertAppc(data.currency.appcValue)
    } else {
      Observable.just(FiatValue(BigDecimal.ZERO, ""))
    }
  }

  private fun handlePaymentMethodSelected() {
    disposables.add(
        view.getPaymentMethodClick()
            .doOnNext { view.paymentMethodsFocusRequest() }
            .subscribe())
  }

  private fun loadBonusIntoView(appPackage: String, amount: String,
                                currency: String): Observable<ForecastBonus> {
    return interactor.convertLocal(currency, amount, 18)
        .flatMapSingle { interactor.getEarningBonus(appPackage, it.amount) }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnNext {
          if (it.status != ForecastBonus.Status.ACTIVE || it.amount <= BigDecimal.ZERO) {
            view.hideBonus()
          } else {
            view.showBonus(it.amount, it.currency)
          }
          view.setNextButtonState(true)
          gamificationLevel = it.level
        }
        .map { ForecastBonus(it.status, it.amount, it.currency) }
  }

  private fun handleInsertedValue(packageName: String, topUpData: TopUpData,
                                  limitValues: TopUpLimitValues): Observable<ForecastBonus> {
    view.setNextButtonState(false)
    if (topUpData.currency.fiatValue != DEFAULT_VALUE && !limitValues.error.hasError) {
      showValueWarning(limitValues.maxValue, limitValues.minValue,
          BigDecimal(topUpData.currency.fiatValue))
    } else {
      handleInvalidFormatInput()
    }
    return handleShowBonus(packageName, topUpData, limitValues,
        BigDecimal(topUpData.currency.fiatValue))
  }

  private fun handleShowBonus(appPackage: String, topUpData: TopUpData,
                              limitValues: TopUpLimitValues,
                              amount: BigDecimal): Observable<ForecastBonus> {
    return if (!limitValues.error.hasError && (amount < limitValues.minValue.amount || amount > limitValues.maxValue.amount)) {
      view.hideBonus()
      view.changeMainValueColor(false)
      view.setNextButtonState(false)
      Observable.empty()
    } else {
      view.changeMainValueColor(true)
      loadBonusIntoView(appPackage, topUpData.currency.fiatValue,
          topUpData.currency.fiatCurrencyCode)
    }
  }

  private fun handleRetryClick() {
    disposables.add(view.retryClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showRetryAnimation() }
        .delay(1, TimeUnit.SECONDS)
        .doOnNext {
          setupUi()
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun showValueWarning(maxValue: FiatValue, minValue: FiatValue, amount: BigDecimal) {
    val localCurrency = " ${maxValue.currency}"
    when {
      amount == BigDecimal(-1) -> {
        view.hideValueInputWarning()
        Log.w("TopUpFragmentPresenter", "Unable to retrieve values")
      }
      amount > maxValue.amount -> view.showMaxValueWarning(
          maxValue.amount.toPlainString() + localCurrency)
      amount < minValue.amount -> view.showMinValueWarning(
          minValue.amount.toPlainString() + localCurrency)
      else -> view.hideValueInputWarning()
    }
  }

  private fun isValueInRange(limitValues: TopUpLimitValues, value: Double): Boolean {
    return limitValues.error.hasError || limitValues.minValue.amount.toDouble() <= value &&
        limitValues.maxValue.amount.toDouble() >= value
  }

  private fun isCurrencyValid(currencyData: CurrencyData): Boolean {
    return currencyData.appcValue != DEFAULT_VALUE && currencyData.fiatValue != DEFAULT_VALUE
  }

  private fun updateConversionValue(value: BigDecimal, topUpData: TopUpData): TopUpData {
    if (topUpData.selectedCurrency == TopUpData.FIAT_CURRENCY) {
      topUpData.currency.appcValue =
          if (value == BigDecimal.ZERO) DEFAULT_VALUE else value.toString()
    } else {
      topUpData.currency.fiatValue =
          if (value == BigDecimal.ZERO) DEFAULT_VALUE else value.toString()
    }
    return topUpData
  }

  private fun isConvertedValueAvailable(data: TopUpData): Boolean {
    return if (data.selectedCurrency == TopUpData.FIAT_CURRENCY) {
      data.currency.appcValue != DEFAULT_VALUE
    } else {
      data.currency.fiatValue != DEFAULT_VALUE
    }
  }

  private fun handleValuesClicks() {
    disposables.add(view.getValuesClicks()
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .doOnNext { view.disableSwapCurrencyButton() }
        .doOnNext {
          if (view.getSelectedCurrency() == TopUpData.FIAT_CURRENCY) {
            view.changeMainValueText(it.amount.toString())
          } else {
            handleChipCreditsInput(it.currency, it.amount)
          }
        }
        .debounce(300, TimeUnit.MILLISECONDS, viewScheduler)
        .doOnNext { view.enableSwapCurrencyButton() }
        .subscribe())
  }

  private fun handleChipCreditsInput(currency: String, amount: BigDecimal) {
    disposables.add(interactor.convertLocal(currency, amount.toString(), 2)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnNext { view.changeMainValueText(it.amount.toString()) }
        .doOnError { view.showNoNetworkError() }
        .subscribe({}, { it.printStackTrace() }))
  }
}
