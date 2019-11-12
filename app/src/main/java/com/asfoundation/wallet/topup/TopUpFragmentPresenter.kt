package com.asfoundation.wallet.topup

import com.appcoins.wallet.gamification.repository.ForecastBonus
import com.asfoundation.wallet.topup.TopUpData.Companion.DEFAULT_VALUE
import com.asfoundation.wallet.topup.paymentMethods.PaymentMethodData
import com.asfoundation.wallet.ui.iab.FiatValue
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

  companion object {
    private const val NUMERIC_REGEX = "^([1-9]|[0-9]+[,.]+[0-9])[0-9]*?\$"

    //Preselects the second chip
    private const val PRESELECTED_CHIP = 1
  }

  fun present(appPackage: String, initialSetup: Boolean) {
    setupUi(initialSetup)
    handleChangeCurrencyClick()
    handleNextClick()
    handleManualAmountChange(appPackage)
    handlePaymentMethodSelected()
    handleDefaultValueChips()
    handleValueChipsClick()
  }

  fun stop() {
    interactor.cleanCachedValues()
    disposables.dispose()
  }

  private fun setupUi(initialSetup: Boolean) {
    disposables.add(Single.zip(
        interactor.getPaymentMethods().subscribeOn(networkScheduler).observeOn(viewScheduler),
        interactor.getLimitTopUpValues().subscribeOn(networkScheduler).observeOn(viewScheduler),
        BiFunction { paymentMethods: List<PaymentMethodData>, values: TopUpLimitValues ->
          view.setupUiElements(filterPaymentMethods(paymentMethods),
              LocalCurrency(values.maxValue.symbol, values.maxValue.currency))
        })
        .subscribe({
          if (initialSetup) {
            handlePreselectedChip()
          }
        }, { throwable -> throwable.printStackTrace() }))
  }

  private fun handleChangeCurrencyClick() {
    disposables.add(view.getChangeCurrencyClick()
        .doOnNext {
          view.toggleSwitchCurrencyOn()
          view.rotateChangeCurrencyButton()
          view.switchCurrencyData()
          view.toggleSwitchCurrencyOff()
          if (view.getSelectedCurrency() == TopUpData.FIAT_CURRENCY && view.getSelectedChip() != -1) {
            view.selectChip(view.getSelectedChip())
          }
          if (view.getSelectedCurrency() == TopUpData.APPC_C_CURRENCY) {
            view.setUnselectedChipsBackground()
          }
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
              if (view.getSelectedCurrency() == TopUpData.APPC_C_CURRENCY) {
                view.deselectChips()
              }
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
              topUpData.selectedCurrency, "BDS",
              "TOPUP", topUpData.bonusValue, view.getSelectedChip(), it, view.getChipAvailability())
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
    view.deselectChips()
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
      if (topUpData.currency.fiatValue != DEFAULT_VALUE) {
        if (topUpData.selectedCurrency == TopUpData.FIAT_CURRENCY) {
          handleManualInputValue(topUpData)
        } else {
          view.deselectChips()
        }
      } else {
        handleEmptyOrDefaultInput()
      }
    } else {
      handleInvalidFormatInput()
    }
  }

  private fun handlePreselectedChip() {
    disposables.add(getChipValue(PRESELECTED_CHIP)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.initialInputSetup(PRESELECTED_CHIP, it.amount) }
        .subscribe())
  }

  private fun handleManualInputValue(topUpData: TopUpData) {
    disposables.add(interactor.getChipIndex(
        FiatValue(BigDecimal(topUpData.currency.fiatValue), topUpData.currency.fiatCurrencyCode,
            topUpData.currency.fiatCurrencySymbol))
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          if (it != -1) {
            view.selectChip(it)
          } else {
            view.deselectChips()
          }
        }
        .subscribe())
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
            view.setNextButtonState(true)
          }
        }
  }

  private fun handleInsertedValue(packageName: String, topUpData: TopUpData,
                                  limitValues: TopUpLimitValues): Observable<ForecastBonus> {
    view.setNextButtonState(false)
    if (topUpData.currency.fiatValue != DEFAULT_VALUE) {
      showValueWarning(limitValues.maxValue, limitValues.minValue,
          BigDecimal(topUpData.currency.fiatValue))
    } else {
      handleInvalidFormatInput()
    }
    return handleShowBonus(packageName, topUpData, limitValues.maxValue, limitValues.minValue,
        BigDecimal(topUpData.currency.fiatValue))
  }

  private fun handleShowBonus(appPackage: String, topUpData: TopUpData, maxValue: FiatValue,
                              minValue: FiatValue, amount: BigDecimal): Observable<ForecastBonus> {
    return if (amount < minValue.amount || amount > maxValue.amount) {
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

  private fun handleDefaultValueChips() {
    disposables.add(interactor.getDefaultValues()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.setupDefaultValueChips(it) }
        .subscribe())
  }

  private fun handleValueChipsClick() {
    disposables.add(view.getChipsClick()
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .doOnNext { index ->
          view.deselectChips()
          view.selectChip(index)
          view.disableSwapCurrencyButton()
        }
        .flatMapSingle {
          getChipValue(it).subscribeOn(networkScheduler)
              .observeOn(viewScheduler)
        }
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
        .subscribe())
  }

  private fun showValueWarning(maxValue: FiatValue, minValue: FiatValue, amount: BigDecimal) {
    val localCurrency = " ${maxValue.currency}"
    when {
      amount > maxValue.amount -> view.showMaxValueWarning(
          maxValue.amount.toPlainString() + localCurrency)
      amount < minValue.amount -> view.showMinValueWarning(
          minValue.amount.toPlainString() + localCurrency)
      else -> view.hideValueInputWarning()
    }
  }

  private fun getChipValue(index: Int): Single<FiatValue> {
    return interactor.getDefaultValues()
        .map { it[index] }
  }

  private fun isValueInRange(limitValues: TopUpLimitValues, value: Double): Boolean {
    return limitValues.minValue.amount.toDouble() <= value &&
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
}
