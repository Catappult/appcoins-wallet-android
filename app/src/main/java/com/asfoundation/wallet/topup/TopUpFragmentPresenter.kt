package com.asfoundation.wallet.topup

import android.os.Bundle
import com.appcoins.wallet.core.analytics.analytics.legacy.ChallengeRewardAnalytics
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.Log
import com.appcoins.wallet.core.utils.android_common.extensions.isNoNetworkException
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.appcoins.wallet.feature.challengereward.data.ChallengeRewardManager
import com.appcoins.wallet.feature.challengereward.data.model.ChallengeRewardFlowPath
import com.asfoundation.wallet.billing.paypal.usecases.IsPaypalAgreementCreatedUseCase
import com.asfoundation.wallet.billing.paypal.usecases.RemovePaypalBillingAgreementUseCase
import com.asfoundation.wallet.topup.TopUpData.Companion.DEFAULT_VALUE
import com.asfoundation.wallet.ui.iab.PaymentMethodsPresenter
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class TopUpFragmentPresenter(
  private val view: TopUpFragmentView,
  private val activity: TopUpActivityView?,
  private val interactor: TopUpInteractor,
  private val removePaypalBillingAgreementUseCase: RemovePaypalBillingAgreementUseCase,
  private val isPaypalAgreementCreatedUseCase: IsPaypalAgreementCreatedUseCase,
  private val viewScheduler: Scheduler,
  private val networkScheduler: Scheduler,
  val disposables: CompositeDisposable,
  private val topUpAnalytics: TopUpAnalytics,
  private val formatter: CurrencyFormatUtils,
  private val selectedValue: String?,
  private val logger: Logger,
  private val networkThread: Scheduler,
  private val challengeRewardAnalytics: ChallengeRewardAnalytics,
) {

  private var cachedGamificationLevel = 0
  private var hasDefaultValues = false
  var showPayPalLogout: Subject<Boolean> = BehaviorSubject.create()

  companion object {
    private val TAG = TopUpFragmentPresenter::class.java.name
    private const val NUMERIC_REGEX = "^([1-9]|[0-9]+[,.]+[0-9])[0-9]*?\$"
    private const val GAMIFICATION_LEVEL = "gamification_level"
  }

  fun present(appPackage: String, savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      cachedGamificationLevel = savedInstanceState.getInt(GAMIFICATION_LEVEL)
    }
    handlePaypalBillingAgreement()
    setupUi()
    handleChangeCurrencyClick()
    handleNextClick()
    handleRetryClick()
    handleManualAmountChange(appPackage)
    handlePaymentMethodSelected()
    handleValuesClicks()
    handleKeyboardEvents()
  }

  fun stop() {
    interactor.cleanCachedValues()
    disposables.dispose()
  }

  private fun setupUi() {
    disposables.add(
      Single.zip(
        interactor.getLimitTopUpValues()
          .subscribeOn(networkScheduler)
          .observeOn(viewScheduler),
        interactor.getDefaultValues()
          .subscribeOn(networkScheduler)
          .observeOn(viewScheduler)
      ) { values: TopUpLimitValues, defaultValues: TopUpValuesModel ->
        if (values.error.hasError || defaultValues.error.hasError &&
          (values.error.isNoNetwork || defaultValues.error.isNoNetwork)
        ) {
          view.showNoNetworkError()
        } else {
          view.setupCurrency(LocalCurrency(values.maxValue.symbol, values.maxValue.currency))
          updateDefaultValues(defaultValues, 1)
        }
      }
        .subscribe({}, { handleError(it) })
    )
  }

  private fun retrievePaymentMethods(
    fiatAmount: String,
    currency: String,
    packageName: String
  ): Completable =
    interactor.getPaymentMethods(fiatAmount, currency, packageName)
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .doOnSuccess {
        if (it.isNotEmpty()) {
          view.setupPaymentMethods(
            paymentMethods = it
          )
        } else {
          view.showNoMethodsError()
        }
      }
      .ignoreElement()

  private fun updateDefaultValues(
    topUpValuesModel: TopUpValuesModel,
    defaultValueIndex: Int
  ) {
    hasDefaultValues =
      topUpValuesModel.error.hasError.not() && topUpValuesModel.values.size >= 3
    if (hasDefaultValues) {
      val defaultValues = topUpValuesModel.values
      val defaultFiatValue = defaultValues[defaultValueIndex]
      view.setDefaultAmountValue(selectedValue ?: defaultFiatValue.amount.toString())
      view.setValuesAdapter(defaultValues)
    } else {
      view.hideValuesAdapter()
    }
  }

  private fun handleKeyboardEvents() {
    disposables.add(view.getKeyboardEvents()
      .doOnNext {
        if (it && hasDefaultValues) view.showValuesAdapter()
        else view.hideValuesAdapter()
      }
      .subscribeOn(viewScheduler)
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleError(throwable: Throwable) {
    throwable.printStackTrace()
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
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleNextClick() {
    disposables.add(view.getNextClick()
      .throttleFirst(500, TimeUnit.MILLISECONDS)
      .observeOn(networkScheduler)
      .switchMap { topUpData ->
        interactor.getLimitTopUpValues()
          .toObservable()
          .filter {
            isCurrencyValid(topUpData.currency)
                && isValueInRange(it, topUpData.currency.fiatValue.toDouble())
                && topUpData.paymentMethod != null
          }
          .observeOn(viewScheduler)
          .doOnNext {
            topUpAnalytics.sendSelectionEvent(
              value = topUpData.currency.appcValue.toDouble(),
              action = "next",
              paymentMethod = topUpData.paymentMethod!!.paymentType.name
            )
            navigateToPayment(topUpData, cachedGamificationLevel)
          }
      }
      .subscribe({}, { handleError(it) })
    )
  }


  private fun handleManualAmountChange(packageName: String) {
    disposables.add(view.getEditTextChanges()
      .doOnNext { resetValues(it) }
      .debounce(700, TimeUnit.MILLISECONDS, viewScheduler)
      .doOnNext { handleInputValue(it) }
      .filter { isNumericOrEmpty(it) }
      .switchMapCompletable { topUpData ->
        getConvertedValue(topUpData)
          .subscribeOn(networkScheduler)
          .map { value -> updateConversionValue(value.amount, topUpData) }
          .filter { isConvertedValueAvailable(it) }
          .observeOn(viewScheduler)
          .doOnComplete { view.setConversionValue(topUpData) }
          .flatMapCompletable {
            interactor.getLimitTopUpValues()
              .toObservable()
              .subscribeOn(networkScheduler)
              .observeOn(viewScheduler)
              .flatMapCompletable { handleInsertedValue(packageName, topUpData, it) }
          }
          .doOnError { handleError(it) }
          .onErrorComplete()
      }
      .subscribe({}, { it.printStackTrace() })
    )
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

  private fun isNumericOrEmpty(data: TopUpData): Boolean =
    if (data.selectedCurrencyType == TopUpData.FIAT_CURRENCY) {
      data.currency.fiatValue == DEFAULT_VALUE
          || data.currency.fiatValue.matches(NUMERIC_REGEX.toRegex())
    } else {
      data.currency.appcValue == DEFAULT_VALUE
          || data.currency.appcValue.matches(NUMERIC_REGEX.toRegex())
    }

  private fun getConvertedValue(data: TopUpData): Observable<FiatValue> =
    if (data.selectedCurrencyType == TopUpData.FIAT_CURRENCY
      && data.currency.fiatValue != DEFAULT_VALUE
    ) {
      interactor.convertLocal(data.currency.fiatCurrencyCode, data.currency.fiatValue, 2)
        .toObservable()
    } else if (data.selectedCurrencyType == TopUpData.APPC_C_CURRENCY
      && data.currency.appcValue != DEFAULT_VALUE
    ) {
      interactor.convertAppc(data.currency.appcValue).toObservable()
    } else {
      Observable.just(
        FiatValue(
          BigDecimal.ZERO,
          ""
        )
      )
    }

  private fun handlePaymentMethodSelected() {
    disposables.add(view.getPaymentMethodClick()
      .doOnNext {
        view.paymentMethodsFocusRequest()
        setNextButton(it)
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun setNextButton(methodSelected: String?) {
    disposables.add(
      showPayPalLogout
      .subscribe {
        if (methodSelected == PaymentMethodsView.PaymentMethodId.PAYPAL_V2.id && it!!) {
          view.setTopupButton()
        } else {
          view.setNextButton()
        }
      }
    )
  }

  private fun loadBonusIntoView(
    appPackage: String, amount: String,
    currency: String
  ): Completable = interactor.getEarningBonus(appPackage, amount.toBigDecimal(), currency)
    .subscribeOn(networkScheduler)
    .observeOn(viewScheduler)
    .doOnSuccess {
      if (interactor.isBonusValidAndActive(it)) {
        val scaledBonus = formatter.scaleFiat(it.amount)
        view.showBonus(scaledBonus, it.currency)
      } else {
        view.removeBonus()
      }
      view.setNextButtonState(true)
      cachedGamificationLevel = it.level
    }
    .ignoreElement()

  private fun handleInsertedValue(
    packageName: String, topUpData: TopUpData,
    limitValues: TopUpLimitValues
  ): Completable {
    view.setNextButtonState(false)
    val fiatAmount = BigDecimal(topUpData.currency.fiatValue)
    if (topUpData.currency.fiatValue != DEFAULT_VALUE && !limitValues.error.hasError) {
      handleValueWarning(limitValues.maxValue, limitValues.minValue, fiatAmount)
    } else {
      handleInvalidFormatInput()
    }
    return updateUiInformation(
      packageName, limitValues,
      topUpData.currency.fiatValue, topUpData.currency.fiatCurrencyCode
    )
  }

  private fun updateUiInformation(
    appPackage: String,
    limitValues: TopUpLimitValues, fiatAmount: String,
    currency: String
  ): Completable =
    if (isValueInRange(limitValues, fiatAmount.toDouble())) {
      view.changeMainValueColor(true)
      view.hidePaymentMethods()
      if (interactor.isBonusValidAndActive()) view.showBonusSkeletons()
      retrievePaymentMethods(fiatAmount, currency, appPackage)
        .andThen(loadBonusIntoView(appPackage, fiatAmount, currency))
    } else {
      view.hideBonusAndSkeletons()
      view.changeMainValueColor(false)
      view.setNextButtonState(false)
      Completable.complete()
    }

  private fun handleRetryClick() {
    disposables.add(view.retryClick()
      .observeOn(viewScheduler)
      .doOnNext { view.showRetryAnimation() }
      .delay(1, TimeUnit.SECONDS)
      .observeOn(viewScheduler)
      .doOnNext {
        view.showSkeletons()
        setupUi()
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleValueWarning(
    maxValue: FiatValue,
    minValue: FiatValue,
    amount: BigDecimal
  ) {
    val localCurrency = " ${maxValue.currency}"
    when {
      amount == BigDecimal(-1) -> {
        view.hideValueInputWarning()
        Log.w("TopUpFragmentPresenter", "Unable to retrieve values")
      }
      amount > maxValue.amount ->
        view.showMaxValueWarning(maxValue.amount.toPlainString() + localCurrency)
      amount < minValue.amount ->
        view.showMinValueWarning(minValue.amount.toPlainString() + localCurrency)
      else -> view.hideValueInputWarning()
    }
  }

  private fun isValueInRange(limitValues: TopUpLimitValues, value: Double): Boolean =
    limitValues.error.hasError
        || limitValues.minValue.amount.toDouble() <= value
        && limitValues.maxValue.amount.toDouble() >= value

  private fun isCurrencyValid(currencyData: CurrencyData): Boolean =
    currencyData.appcValue != DEFAULT_VALUE && currencyData.fiatValue != DEFAULT_VALUE

  private fun updateConversionValue(
    value: BigDecimal,
    topUpData: TopUpData
  ): TopUpData {
    if (topUpData.selectedCurrencyType == TopUpData.FIAT_CURRENCY) {
      topUpData.currency.appcValue =
        if (value == BigDecimal.ZERO) DEFAULT_VALUE else value.toString()
    } else {
      topUpData.currency.fiatValue =
        if (value == BigDecimal.ZERO) DEFAULT_VALUE else value.toString()
    }
    return topUpData
  }

  private fun isConvertedValueAvailable(data: TopUpData): Boolean =
    if (data.selectedCurrencyType == TopUpData.FIAT_CURRENCY) {
      data.currency.appcValue != DEFAULT_VALUE
    } else {
      data.currency.fiatValue != DEFAULT_VALUE
    }

  private fun handleValuesClicks() {
    disposables.add(view.getValuesClicks()
      .throttleFirst(50, TimeUnit.MILLISECONDS)
      .doOnNext { view.disableSwapCurrencyButton() }
      .doOnNext {
        if (view.getSelectedCurrency() == TopUpData.FIAT_CURRENCY) {
          view.changeMainValueText(it.amount.toString())
        } else {
          convertAndChangeMainValue(it.currency, it.amount)
        }
      }
      .debounce(300, TimeUnit.MILLISECONDS, viewScheduler)
      .doOnNext { view.enableSwapCurrencyButton() }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun convertAndChangeMainValue(currency: String, amount: BigDecimal) {
    disposables.add(interactor.convertLocal(currency, amount.toString(), 2)
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .doOnSuccess { view.changeMainValueText(it.amount.toString()) }
      .doOnError { handleError(it) }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  fun removePaypalBillingAgreement() {
    disposables.add(
      removePaypalBillingAgreementUseCase.invoke()
        .subscribeOn(networkThread)
        .observeOn(viewScheduler)
        .subscribe(
          {
            view.hideLoading()
            android.util.Log.d(PaymentMethodsPresenter.TAG, "Agreement removed")
          },
          {
            view.hideLoading()
            logger.log(PaymentMethodsPresenter.TAG, "Agreement Not Removed")
          }
        )
    )
  }

  private fun handlePaypalBillingAgreement() {
    disposables.add(
      isPaypalAgreementCreatedUseCase()
        .subscribeOn(networkScheduler)
        .subscribe(
          {
            showPayPalLogout.onNext(it!!)
          },
          {
            logger.log(TAG, "Error getting agreement")
            showPayPalLogout.onNext(false)
          }
        )
    )
  }

  private fun navigateToPayment(topUpData: TopUpData, gamificationLevel: Int) {
    val paymentMethod = topUpData.paymentMethod!!
    if (paymentMethod.paymentType == PaymentType.CARD
      || paymentMethod.paymentType == PaymentType.PAYPAL
      || paymentMethod.paymentType == PaymentType.GIROPAY
    ) {
      activity?.navigateToAdyenPayment(
        paymentType = paymentMethod.paymentType,
        data = mapTopUpPaymentData(topUpData, gamificationLevel)
      )
    } else if (paymentMethod.paymentType == PaymentType.LOCAL_PAYMENTS) {
      activity?.navigateToLocalPayment(
        paymentId = paymentMethod.paymentId,
        icon = paymentMethod.icon,
        label = paymentMethod.label,
        async = paymentMethod.async,
        topUpData = mapTopUpPaymentData(topUpData, gamificationLevel)
      )
    } else if (paymentMethod.paymentType == PaymentType.PAYPALV2) {
      activity?.navigateToPaypalV2(
        paymentType = paymentMethod.paymentType,
        data = mapTopUpPaymentData(topUpData, gamificationLevel)
      )
    } else if (paymentMethod.paymentType == PaymentType.CHALLENGE_REWARD) {
      challengeRewardAnalytics.sendChallengeRewardEvent(ChallengeRewardFlowPath.TOPUP.id)
      ChallengeRewardManager.onNavigate()
    }
  }

  private fun mapTopUpPaymentData(
    topUpData: TopUpData,
    gamificationLevel: Int
  ): TopUpPaymentData =
    TopUpPaymentData(
      fiatValue = topUpData.currency.fiatValue,
      fiatCurrencyCode = topUpData.currency.fiatCurrencyCode,
      selectedCurrencyType = topUpData.selectedCurrencyType,
      bonusValue = topUpData.bonusValue,
      fiatCurrencySymbol = topUpData.currency.fiatCurrencySymbol,
      appcValue = topUpData.currency.appcValue,
      transactionType = "TOPUP",
      gamificationLevel = gamificationLevel
    )

  fun onSavedInstance(outState: Bundle) =
    outState.putInt(GAMIFICATION_LEVEL, cachedGamificationLevel)
}
