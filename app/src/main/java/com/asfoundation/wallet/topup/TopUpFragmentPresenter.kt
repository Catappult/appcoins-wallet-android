package com.asfoundation.wallet.topup

import android.annotation.SuppressLint
import android.os.Bundle
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.network.backend.model.enums.RefundDisclaimerEnum
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.Log
import com.appcoins.wallet.core.utils.android_common.extensions.isNoNetworkException
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetShowRefundDisclaimerCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SetCachedShowRefundDisclaimerUseCase
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.appcoins.wallet.sharedpreferences.CardPaymentDataSource
import com.asfoundation.wallet.billing.adyen.PaymentBrands
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.amazonPay.usecases.DeleteAmazonPayChargePermissionUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.GetAmazonPayChargePermissionLocalStorageUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.GetAmazonPayChargePermissionUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.SaveAmazonPayChargePermissionLocalStorageUseCase
import com.asfoundation.wallet.billing.paypal.usecases.IsPaypalAgreementCreatedUseCase
import com.asfoundation.wallet.billing.paypal.usecases.RemovePaypalBillingAgreementUseCase
import com.asfoundation.wallet.manage_cards.models.StoredCard
import com.asfoundation.wallet.manage_cards.usecases.GetStoredCardsUseCase
import com.asfoundation.wallet.topup.TopUpData.Companion.DEFAULT_VALUE
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.asfoundation.wallet.ui.iab.PaymentMethodFee
import com.asfoundation.wallet.ui.iab.PaymentMethodsView.PaymentMethodId
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
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
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
  private val getStoredCardsUseCase: GetStoredCardsUseCase,
  private val cardPaymentDataSource: CardPaymentDataSource,
  private val getCurrentWalletUseCase: GetCurrentWalletUseCase,
  private val getShowRefundDisclaimerCodeUseCase: GetShowRefundDisclaimerCodeUseCase,
  private val setCachedShowRefundDisclaimerUseCase: SetCachedShowRefundDisclaimerUseCase,
  private val getAmazonPayChargePermissionLocalStorageUseCase: GetAmazonPayChargePermissionLocalStorageUseCase,
  private val saveAmazonPayChargePermissionLocalStorageUseCase: SaveAmazonPayChargePermissionLocalStorageUseCase,
  private val deleteAmazonPayChargePermissionUseCase: DeleteAmazonPayChargePermissionUseCase,
  private val getAmazonPayChargePermissionUseCase: GetAmazonPayChargePermissionUseCase
) {

  private var cachedGamificationLevel = 0
  private var hasDefaultValues = false
  var showPayPalLogout: Boolean = false
  var showAmazonLogout: Boolean = false
  private var firstPaymentMethodsFetch: Boolean = true
  var hasStoredCard: Boolean = false
  var storedCardID: String? = null

  companion object {
    private val TAG = TopUpFragmentPresenter::class.java.name
    private const val NUMERIC_REGEX = "^([1-9]|[0-9]+[,.]+[0-9])[0-9]*?\$"
    private const val GAMIFICATION_LEVEL = "gamification_level"
  }

  fun present(appPackage: String, savedInstanceState: Bundle?) {
    view.lockRotation()
    savedInstanceState?.let {
      cachedGamificationLevel = savedInstanceState.getInt(GAMIFICATION_LEVEL)
    }
    updateRefundDisclaimerValue()
    handlePaypalBillingAgreement()
    getAmazonPayChargePermission()
    setupUi()
    handleNextClick()
    handleRetryClick()
    handleManualAmountChange(appPackage)
    handlePaymentMethodSelected()
    handleValuesClicks()
    handleKeyboardEvents()
    handleChallengeRewardWalletAddress()
    getCardIdSharedPreferences()
  }

  fun stop() {
    interactor.cleanCachedValues()
    disposables.dispose()
  }

  private fun setupUi(paymentMethod: PaymentMethod? = null) {
    disposables.add(
      Single.zip(
        interactor.getLimitTopUpValues(
          currency = paymentMethod?.price?.currency,
          method = paymentMethod?.id
        ),
        interactor.getDefaultValues(currency = paymentMethod?.price?.currency)
      ) { values, defaultValues -> values to defaultValues }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .subscribe({ pair ->
          if ((pair.first.error.hasError || pair.second.error.hasError) &&
            (pair.first.error.isNoNetwork || pair.second.error.isNoNetwork)
          ) {
            view.showNoNetworkError()
          } else {
            view.setupCurrency(LocalCurrency(pair.first.maxValue.symbol, pair.first.maxValue.currency))
            updateDefaultValues(pair.second)
          }
        }, { handleError(it) })
    )
  }

  private fun retrievePaymentMethodsAndLoadBonus(
    fiatAmount: String,
    currency: String,
    appPackage: String
  ): Completable =
    Single.zip(
      interactor.getPaymentMethods(
        value = fiatAmount,
        currency = currency
      ),
      getStoredCardsUseCase()
    ) { paymentMethods, storedCards -> Pair(paymentMethods, storedCards) }
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .doOnSuccess { (paymentMethods, storedCards) ->
        if (paymentMethods.isNotEmpty()) {
          val cardList = storedCards.map {
            StoredCard(
              cardLastNumbers = it.lastFour ?: "****",
              cardIcon = PaymentBrands.getPayment(it.brand).brandFlag,
              recurringReference = it.id,
              isSelectedCard = !storedCardID.isNullOrEmpty() && it.id == storedCardID
            )
          }
          val selectedPaymentMethod = getSelectedPaymentMethod(paymentMethods)
          val selectedCurrency = getCurrencyOfSelectedPaymentMethod(paymentMethods)
          view.setupPaymentMethods(
            paymentMethods = paymentMethods,
            cardsList = cardList
          )
          handleFeeVisibility(selectedPaymentMethod.fee)
          if (selectedCurrency != view.getSelectedCurrency().code) {
            setupUi(selectedPaymentMethod)
          } else {
            view.hideValuesSkeletons()
            loadBonusIntoView(appPackage, fiatAmount, selectedCurrency)
          }
        } else {
          view.showNoMethodsError()
        }
        firstPaymentMethodsFetch = false
      }
      .ignoreElement()

  private fun loadBonusIntoView(
    appPackage: String,
    amount: String,
    currency: String
  ) {
    interactor.getEarningBonus(appPackage, amount.toBigDecimal(), currency)
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .doOnSuccess { bonusAndLevel ->
        if (activity != null && activity.isActivityActive()) handleBonus(bonusAndLevel)
        cachedGamificationLevel = bonusAndLevel.level
      }
      .subscribe()
  }

  private fun updateRefundDisclaimerValue() {
    disposables.add(
      getShowRefundDisclaimerCodeUseCase().subscribeOn(networkScheduler).observeOn(viewScheduler)
        .doOnSuccess {
          if (it.showRefundDisclaimer == RefundDisclaimerEnum.SHOW_REFUND_DISCLAIMER.state) {
            setCachedShowRefundDisclaimerUseCase(true)
            view.changeVisibilityRefundDisclaimer(true)
          } else {
            setCachedShowRefundDisclaimerUseCase(false)
            view.changeVisibilityRefundDisclaimer(false)
          }
        }.subscribe({}, {
          it.printStackTrace()
        })
    )
  }


  private fun handleBonus(bonusAndLevel: ForecastBonusAndLevel) {
    if (interactor.isBonusValidAndActive(bonusAndLevel)) {
      val scaledBonus = formatter.scaleFiat(bonusAndLevel.amount)
      if (view.getCurrentPaymentMethod()?.id == PaymentMethodId.CHALLENGE_REWARD.id) {
        view.hideBonusAndSkeletons()
      } else {
        view.showBonus(scaledBonus, bonusAndLevel.currency)
      }
    } else {
      view.removeBonus()
    }
    view.setNextButtonState(true)
  }

  private fun updateDefaultValues(
    topUpValuesModel: TopUpValuesModel,
    defaultValueIndex: Int = 1
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
    if (throwable.isNoNetworkException()) view.showNoNetworkError() else view.showNoMethodsError()
  }

  private fun handleNextClick() {
    disposables.add(view.getNextClick()
      .throttleFirst(500, TimeUnit.MILLISECONDS)
      .observeOn(networkScheduler)
      .switchMap { topUpData ->
        interactor.getLimitTopUpValues(
          currency = topUpData.currency.fiatCurrencyCode,
          method = view.getCurrentPaymentMethod()?.id
        )
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
              action = BillingAnalytics.ACTION_NEXT,
              paymentMethod = topUpData.paymentMethod!!.paymentType.name
            )
            navigateToPayment(topUpData, cachedGamificationLevel, false)
          }
      }
      .subscribe({}, { handleError(it) })
    )
  }

  fun handleNewCardActon(topUpData: TopUpData) {
    disposables.add(interactor.getLimitTopUpValues(
      currency = topUpData.currency.fiatCurrencyCode,
      method = view.getCurrentPaymentMethod()?.id
    )
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
          action = BillingAnalytics.ACTION_NEXT,
          paymentMethod = topUpData.paymentMethod!!.paymentType.name
        )
        navigateToPayment(topUpData, cachedGamificationLevel, true)
      }.subscribe({}, { handleError(it) })
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
            interactor.getLimitTopUpValues(
              currency = topUpData.currency.fiatCurrencyCode,
              method = view.getCurrentPaymentMethod()?.id
            )
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
    disposables.add(
      view.getPaymentMethodClick()
        .distinctUntilChanged()
        .doOnError { it.printStackTrace() }
        .subscribe { paymentMethod ->
          if (paymentMethod.id == PaymentMethodId.CHALLENGE_REWARD.id)
            view.hideBonus()
          else
            view.showBonus()

          view.paymentMethodsFocusRequest()
          setNextButton(paymentMethod.id)
          reloadUiByCurrency(paymentMethod)
          handleFeeVisibility(paymentMethod.fee)
        }
    )
  }

  private fun handleFeeVisibility(fee: PaymentMethodFee?) {
    view.showFee(fee != null && fee.isValidFee())
  }

  private fun reloadUiByCurrency(paymentMethod: PaymentMethod) {
    view.showValuesSkeletons()
    setupUi(paymentMethod)
  }

  private fun setNextButton(methodSelected: String?) {
    when (methodSelected) {
      PaymentMethodId.CREDIT_CARD.id -> {
        if (hasStoredCard) view.setBuyButton()
      }

      PaymentMethodId.PAYPAL_V2.id -> {
        handleLogoutResult(showPayPalLogout)
      }

      PaymentMethodId.AMAZONPAY.id -> {
        handleLogoutResult(showAmazonLogout)
      }

      else -> view.setNextButton()
    }
  }

  private fun handleLogoutResult(logout: Boolean?) {
    if (logout == true) {
      view.setTopupButton()
    } else {
      view.setNextButton()
    }
  }

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
      if (firstPaymentMethodsFetch) view.hidePaymentMethods()
      if (interactor.isBonusValidAndActive()) view.showBonusSkeletons()
      retrievePaymentMethodsAndLoadBonus(
        fiatAmount = fiatAmount,
        currency = currency,
        appPackage = appPackage
      )
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
        setupUi(view.getCurrentPaymentMethod())
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
      .doOnNext {
        if (view.getSelectedCurrencyType() == TopUpData.FIAT_CURRENCY) {
          view.changeMainValueText(it.amount.toString())
        } else {
          convertAndChangeMainValue(it.currency, it.amount)
        }
      }
      .debounce(300, TimeUnit.MILLISECONDS, viewScheduler)
      .doOnNext { }
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
            logger.log(TAG, "Agreement removed")
          },
          {
            view.hideLoading()
            logger.log(TAG, "Agreement Not Removed")
          }
        )
    )
  }

  private fun handlePaypalBillingAgreement() {
    disposables.add(
      isPaypalAgreementCreatedUseCase()
        .subscribeOn(networkScheduler)
        .doOnError { handleError(it) }
        .subscribe(
          {
            showPayPalLogout = it
          },
          {
            logger.log(TAG, "Error getting agreement")
            showPayPalLogout = false
          }
        )
    )
  }

  @SuppressLint("CheckResult")
  private fun getAmazonPayChargePermission() {
    showAmazonLogout = getAmazonPayChargePermissionLocalStorageUseCase().isNotEmpty()
    if (!showAmazonLogout) {
      getAmazonPayChargePermissionUseCase()
        .flatMapCompletable { chargePermissionId ->
          saveAmazonPayChargePermissionLocalStorageUseCase(chargePermissionId = chargePermissionId.chargePermissionId)
          showAmazonLogout = chargePermissionId.chargePermissionId?.isNotEmpty() == true
          Completable.complete()
        }.subscribe({}, {})
    }
  }

  fun removeAmazonPayChargePermission() {
    disposables.add(
      deleteAmazonPayChargePermissionUseCase.invoke()
        .subscribeOn(networkThread)
        .observeOn(viewScheduler)
        .subscribe(
          {
            saveAmazonPayChargePermissionLocalStorageUseCase("")
            view.hideLoading()
            logger.log(TAG, "Charge Permission removed")
          },
          {
            view.hideLoading()
            logger.log(TAG, "Charge Permission not Removed")
          }
        )
    )
  }

  private fun handleChallengeRewardWalletAddress() {
    disposables.add(
      getWalletInfoUseCase(null, false)
        .subscribeOn(networkScheduler)
        .subscribe(
          { activity?.createChallengeReward(it.wallet) },
          { logger.log(TAG, "Error creating challenge reward") }
        )
    )
  }

  fun setCardIdSharedPreferences(recurringReference: String) {
    disposables.add(
      getCurrentWalletUseCase()
        .subscribeOn(networkScheduler)
        .subscribe(
          { cardPaymentDataSource.setPreferredCardId(recurringReference, it.address) },
          { }
        )
    )
  }

  private fun getCardIdSharedPreferences() {
    disposables.add(
      getCurrentWalletUseCase()
        .subscribeOn(networkScheduler)
        .subscribe(
          { storedCardID = cardPaymentDataSource.getPreferredCardId(it.address) },
          { }
        )
    )
  }

  private fun getCurrencyOfSelectedPaymentMethod(paymentMethods: List<PaymentMethod>) =
    getSelectedPaymentMethod(paymentMethods).price.currency

  private fun getSelectedPaymentMethod(paymentMethods: List<PaymentMethod>) =
    paymentMethods.firstOrNull { it.id == view.getCurrentPaymentMethod()?.id }
      ?: paymentMethods.first()

  private fun navigateToPayment(
    topUpData: TopUpData,
    gamificationLevel: Int,
    isNewCardPayment: Boolean
  ) {
    val paymentMethod = topUpData.paymentMethod!!
    when (paymentMethod.paymentType) {
      PaymentType.CARD, PaymentType.PAYPAL -> {
        activity?.navigateToAdyenPayment(
          paymentType = paymentMethod.paymentType,
          data = mapTopUpPaymentData(topUpData, gamificationLevel),
          buyWithStoredCard =
          if (paymentMethod.paymentType == PaymentType.CARD) {
            if (isNewCardPayment) {
              false
            } else {
              hasStoredCard
            }
          } else {
            false
          }
        )
      }

      PaymentType.LOCAL_PAYMENTS -> {
        activity?.navigateToLocalPayment(
          paymentId = paymentMethod.paymentId,
          icon = paymentMethod.icon,
          label = paymentMethod.label,
          async = paymentMethod.async,
          topUpData = mapTopUpPaymentData(topUpData, gamificationLevel)
        )
      }

      PaymentType.PAYPALV2 -> {
        activity?.navigateToPaypalV2(
          paymentType = paymentMethod.paymentType,
          data = mapTopUpPaymentData(topUpData, gamificationLevel)
        )
      }

      PaymentType.VKPAY -> {
        activity?.navigateToVkPayPayment(mapTopUpPaymentData(topUpData, gamificationLevel))
      }

      PaymentType.AMAZONPAY -> {
        activity?.navigateToAmazonPay(mapTopUpPaymentData(topUpData, gamificationLevel))
      }

      PaymentType.GOOGLEPAY_WEB -> {
        activity?.navigateToGooglePay(
          paymentType = paymentMethod.paymentType,
          data = mapTopUpPaymentData(topUpData, gamificationLevel)
        )
      }

      PaymentType.TRUE_LAYER -> {
        activity?.navigateToTrueLayer(
          paymentType = paymentMethod.paymentType,
          data = mapTopUpPaymentData(topUpData, gamificationLevel)
        )
      }

      PaymentType.CHALLENGE_REWARD -> {
        activity?.navigateToChallengeReward()
      }

      else -> {}
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
