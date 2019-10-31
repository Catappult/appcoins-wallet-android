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
    private const val NUMERIC_REGEX = "-?\\d+(\\.\\d+)?"

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
        .doOnSuccess {
          if (initialSetup) {
            handlePreselectedChip()
          }
        }
        .subscribe({ }, { throwable -> throwable.printStackTrace() }))
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
              it.currency.appcValue != DEFAULT_VALUE && it.currency.fiatValue != DEFAULT_VALUE &&
                  limitValues.minValue.amount.toDouble() <= it.currency.fiatValue.toDouble() &&
                  limitValues.minValue.amount.toDouble() <= it.currency.fiatValue.toDouble()
            }
            .doOnNext {
              if (view.getSelectedCurrency() == TopUpData.APPC_C_CURRENCY) {
                view.unselectChips()
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
        .throttleLatest(700, TimeUnit.MILLISECONDS, viewScheduler, true)
        .doOnNext { handleInputValue(it) }
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
                if (topUpData.currency.fiatValue != DEFAULT_VALUE) {
                  handleInsertedValue(packageName, topUpData)
                }
              }
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
    view.unselectChips()
    view.setNextButtonState(false)
  }

  private fun handleInputValue(topUpData: TopUpData) {
    if (isNumericOrEmpty(topUpData)) {
      view.setNextButtonState(false)
      if (topUpData.currency.fiatValue != "--") {
        if (topUpData.selectedCurrency == TopUpData.FIAT_CURRENCY) {
          handleManualInputValue(topUpData)
        } else {
          view.unselectChips()
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
        .doOnSuccess {
          view.initialInputSetup(PRESELECTED_CHIP, it.amount)
        }
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
            view.unselectChips()
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
          }
        }
  }

  private fun handleInsertedValue(packageName: String, topUpData: TopUpData) {
    if (isNumericOrEmpty(topUpData)) {
      val value =
          FiatValue(BigDecimal(topUpData.currency.fiatValue), topUpData.currency.fiatCurrencyCode,
              topUpData.currency.fiatCurrencySymbol)
      disposables.add(interactor.getLimitTopUpValues()
          .subscribeOn(networkScheduler)
          .observeOn(viewScheduler)
          .doOnSuccess {
            view.setNextButtonState(false)
            showValueWarning(it.maxValue, it.minValue, value)
            handleShowBonus(packageName, topUpData, it.maxValue, it.minValue, value)
          }
          .subscribe())
    } else {
      handleInvalidFormatInput()
    }
  }

  private fun handleShowBonus(appPackage: String, topUpData: TopUpData, maxValue: FiatValue,
                              minValue: FiatValue, value: FiatValue) {
    if (value.amount < minValue.amount || value.amount > maxValue.amount) {
      view.hideBonus()
      view.changeMainValueColor(false)
      view.setNextButtonState(false)
    } else {
      view.changeMainValueColor(true)
      view.setNextButtonState(true)
      disposables.add(loadBonusIntoView(appPackage, topUpData.currency.fiatValue,
          topUpData.currency.fiatCurrencyCode)
          .subscribe())
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
          view.unselectChips()
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
        .doOnNext { view.changeMainValueText(it.amount.toString()) }
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

  private fun getChipValue(index: Int): Single<FiatValue> {
    return interactor.getDefaultValues()
        .map { it[index] }
  }
}
