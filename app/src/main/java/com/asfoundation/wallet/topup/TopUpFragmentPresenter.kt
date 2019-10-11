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

  private lateinit var chipValuesList: List<FiatValue>

  companion object {
    private const val NUMERIC_REGEX = "-?\\d+(\\.\\d+)?"

    //Preselects the second chip
    private const val PRESELECTED_CHIP = 1
  }

  fun present(appPackage: String, initialSetup: Boolean) {
    chipValuesList = getChipValuesList()
    setupUi(initialSetup)
    handleChangeCurrencyClick()
    handleNextClick()
    handleManualAmountChange(appPackage)
    handlePaymentMethodSelected()
    handleDefaultValueChips()
    handleValueChipsClick()
  }

  fun stop() {
    disposables.dispose()
  }

  private fun setupUi(initialSetup: Boolean) {
    disposables.add(Single.zip(
        interactor.getPaymentMethods().subscribeOn(networkScheduler).observeOn(viewScheduler),
        interactor.getLocalCurrency().subscribeOn(networkScheduler).observeOn(viewScheduler),
        BiFunction { paymentMethods: List<PaymentMethodData>, currency: LocalCurrency ->
          view.setupUiElements(filterPaymentMethods(paymentMethods), currency)
        })
        .doOnSuccess {
          if (initialSetup) {
            handlePreselectedChip()
          }
        }
        .subscribe({ }, { throwable -> throwable.printStackTrace() }))
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
        view.getNextClick()
            .filter {
              it.currency.appcValue != DEFAULT_VALUE && it.currency.fiatValue != DEFAULT_VALUE
            }.doOnNext {
              view.showLoading()
              val values = getChipValuesList()
              activity?.navigateToPayment(it.paymentMethod!!, it, it.selectedCurrency, "BDS",
                  "TOPUP", it.bonusValue, view.getSelectedChip(), values)
            }.subscribe())
  }

  private fun handleManualAmountChange(packageName: String) {
    disposables.add(view.getEditTextChanges().filter { isNumericOrEmpty(it) }
        .doOnNext {
          view.setNextButtonState(false)
          handleManualInputValue(it)
        }
        .debounce(700, TimeUnit.MILLISECONDS)
        .switchMap { topUpData ->
          getConvertedValue(topUpData)
              .subscribeOn(networkScheduler)
              .map { value ->
                if (topUpData.selectedCurrency == TopUpData.FIAT_CURRENCY) {
                  topUpData.currency.appcValue =
                      if (value.amount == BigDecimal.ZERO) DEFAULT_VALUE else value.amount.toString()
                } else {
                  topUpData.currency.fiatValue =
                      if (value.amount == BigDecimal.ZERO) DEFAULT_VALUE else value.amount.toString()
                }
                return@map topUpData
              }
              .doOnError { it.printStackTrace() }
              .onErrorResumeNext(Observable.empty())
              .observeOn(viewScheduler)
              .filter { it.currency.appcValue != "--" }
              .doOnComplete {
                view.setConversionValue(topUpData)
                handleInvalidValueInput(packageName, topUpData)
              }
        }
        .subscribe())
  }

  private fun handlePreselectedChip() {
    view.initialInputSetup(PRESELECTED_CHIP, getChipValue(PRESELECTED_CHIP).amount.toString())
  }

  private fun handleManualInputValue(topUpData: TopUpData) {
    val chipIndex = getChipIndex(topUpData.currency.fiatValue)
    if (chipIndex != -1) {
      view.selectChip(chipIndex)
    } else {
      view.unselectChips()
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

  private fun hasValidData(data: TopUpData): Boolean {
    return isValidValue(data.currency.fiatValue) &&
        isValidValue(data.currency.appcValue)
        && data.paymentMethod != null
  }

  private fun isValidValue(amount: String): Boolean {
    return amount.matches(NUMERIC_REGEX.toRegex())
        && BigDecimal(amount) > BigDecimal.ZERO
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
        view.getPaymentMethodClick().doOnNext { view.paymentMethodsFocusRequest() }.subscribe())
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
        }
  }

  private fun handleInvalidValueInput(packageName: String, topUpData: TopUpData) {
    if (topUpData.currency.fiatValue != DEFAULT_VALUE) {
      val value =
          FiatValue(BigDecimal(topUpData.currency.fiatValue), topUpData.currency.fiatCurrencyCode,
              topUpData.currency.fiatCurrencySymbol)
      disposables.add(interactor.getLimitTopUpValue()
          .subscribeOn(networkScheduler)
          .observeOn(viewScheduler)
          .doOnSuccess {
            showValueWarning(it.maxValue, it.minValue, value)
            handleShowBonus(packageName, topUpData, it.maxValue, it.minValue, value)
          }
          .subscribe())
    }
  }

  private fun handleShowBonus(appPackage: String, topUpData: TopUpData, maxValue: FiatValue,
                              minValue: FiatValue, value: FiatValue) {
    if (value.amount < minValue.amount || value.amount > maxValue.amount) {
      view.hideBonus()
      view.changeMainValueColor(false)
    } else {
      view.changeMainValueColor(true)
      disposables.add(loadBonusIntoView(appPackage, topUpData.currency.fiatValue,
          topUpData.currency.fiatCurrencyCode)
          .doOnNext { view.setNextButtonState(hasValidData(topUpData)) }
          .subscribe())
    }
  }

  private fun handleDefaultValueChips() {
    disposables.add(interactor.getDefaultValues()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .map { view.setupDefaultValueChips(it) }
        .subscribe())
  }

  private fun handleValueChipsClick() {
    disposables.add(view.getChipsClick()
        .doOnNext { index ->
          view.unselectChips()
          view.selectChip(index)
          view.disableSwapCurrencyButton()
        }
        .map { getChipValue(it) }
        .doOnNext {
          if (view.getSelectedCurrency() == TopUpData.FIAT_CURRENCY) {
            view.changeMainValueText(it.amount.toString())
          } else {
            handleChipCreditsInput(it)
          }
        }
        .debounce(300, TimeUnit.MILLISECONDS, viewScheduler)
        .doOnNext { view.enableSwapCurrencyButton() }
        .subscribe())
  }

  private fun handleChipCreditsInput(value: FiatValue) {
    disposables.add(interactor.convertLocal(value.currency, value.amount.toString(), 2)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .map { view.changeMainValueText(it.amount.toString()) }
        .subscribe())
  }

  private fun showValueWarning(maxValue: FiatValue, minValue: FiatValue, value: FiatValue) {
    val localCurrency = " ${maxValue.currency}"
    if (value.amount > maxValue.amount) {
      view.showMaxValueWarning(maxValue.amount.toPlainString() + localCurrency)
    } else {
      if (value.amount < minValue.amount) {
        view.showMinValueWarning(minValue.amount.toPlainString() + localCurrency)
      } else {
        view.hideValueInputWarning()
      }
    }
  }

  private fun getChipIndex(inputValue: String): Int {
    var index = -1
    if (inputValue != "--") {
      val value = inputValue.toBigDecimal()
      for (i in chipValuesList.indices) {
        if (value == chipValuesList[i].amount) {
          index = i
          break
        }
      }
    }
    return index
  }

  private fun getChipValuesList(): List<FiatValue> {
    return interactor.getDefaultValues()
        .subscribeOn(networkScheduler)
        .blockingGet()
  }

  private fun getChipValue(index: Int): FiatValue {
    return chipValuesList[index]
  }
}
