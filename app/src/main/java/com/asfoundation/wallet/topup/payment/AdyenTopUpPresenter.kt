package com.asfoundation.wallet.topup.payment

import android.os.Bundle
import androidx.annotation.StringRes
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.adyen.AdyenBillingAddress
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.REDIRECT
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2CHALLENGE
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2FINGERPRINT
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.appcoins.wallet.billing.util.Error
import com.asf.wallet.R
import com.asfoundation.wallet.billing.address.BillingAddressModel
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper.Companion.CVC_DECLINED
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper.Companion.FRAUD
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.topup.TopUpData
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.iab.Navigator
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class AdyenTopUpPresenter(private val view: AdyenTopUpView,
                          private val appPackage: String,
                          private val viewScheduler: Scheduler,
                          private val networkScheduler: Scheduler,
                          private val disposables: CompositeDisposable,
                          private val returnUrl: String,
                          private val paymentType: String,
                          private val transactionType: String,
                          private val amount: String,
                          private val currency: String,
                          private val appcValue: String,
                          private val selectedCurrency: String,
                          private val navigator: Navigator,
                          private val billingMessagesMapper: BillingMessagesMapper,
                          private val adyenPaymentInteractor: AdyenPaymentInteractor,
                          private val bonusValue: BigDecimal,
                          private val fiatCurrencySymbol: String,
                          private val adyenErrorCodeMapper: AdyenErrorCodeMapper,
                          private val servicesErrorMapper: ServicesErrorCodeMapper,
                          private val gamificationLevel: Int,
                          private val topUpAnalytics: TopUpAnalytics,
                          private val formatter: CurrencyFormatUtils,
                          private val logger: Logger) {

  private var waitingResult = false
  private var currentError: Int = 0
  private var cachedUid = ""
  private var cachedPaymentData: String? = null
  private var retrievedAmount = amount
  private var retrievedCurrency = currency

  fun present(savedInstanceState: Bundle?) {
    view.setupUi()
    view.showLoading()
    retrieveSavedInstance(savedInstanceState)
    view.setup3DSComponent()
    view.setupRedirectComponent()
    handleViewState(savedInstanceState)
    handleForgetCardClick()
    handleRetryClick(savedInstanceState)
    handleRedirectResponse()
    handleSupportClicks()
    handleTryAgainClicks()
    handleAdyen3DSErrors()
    handlePaymentDetails()
  }

  private fun handleViewState(savedInstanceState: Bundle?) {
    if (currentError != 0) {
      view.showSpecificError(currentError)
      if (paymentType == PaymentType.CARD.name) loadPaymentMethodInfo(savedInstanceState)
    } else {
      if (waitingResult) view.showLoading()
      else loadPaymentMethodInfo(savedInstanceState)
    }
  }

  private fun loadBonusIntoView() {
    if (bonusValue.compareTo(BigDecimal.ZERO) != 0) {
      view.showBonus(bonusValue, fiatCurrencySymbol)
    }
  }

  private fun handleRetryClick(savedInstanceState: Bundle?) {
    disposables.add(view.retryClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showRetryAnimation() }
        .delay(1, TimeUnit.SECONDS)
        .doOnNext {
          if (waitingResult) {
            view.navigateToPaymentSelection()
          } else {
            loadPaymentMethodInfo(savedInstanceState, true)
          }
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleSupportClicks() {
    disposables.add(view.getSupportClicks()
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .flatMapCompletable { adyenPaymentInteractor.showSupport(gamificationLevel) }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleTryAgainClicks() {
    disposables.add(view.getTryAgainClicks()
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .doOnNext {
          if (paymentType == PaymentType.CARD.name) hideSpecificError()
          else view.navigateToPaymentSelection()
        }
        .subscribeOn(viewScheduler)
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun loadPaymentMethodInfo(savedInstanceState: Bundle?, fromError: Boolean = false) {
    disposables.add(convertAmount()
        .flatMap {
          adyenPaymentInteractor.loadPaymentInfo(mapPaymentToService(paymentType), it.toString(),
              currency)
        }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          view.hideLoading()
          if (fromError) view.hideErrorViews()
          if (it.error.hasError) {
            if (it.error.isNetworkError) view.showNetworkError()
            else handleSpecificError(R.string.unknown_error)
          } else {
            val priceAmount = formatter.formatCurrency(it.priceAmount, WalletCurrency.FIAT)
            view.showValues(priceAmount, it.priceCurrency)
            retrievedAmount = it.priceAmount.toString()
            retrievedCurrency = it.priceCurrency
            if (paymentType == PaymentType.CARD.name) {
              view.finishCardConfiguration(it.paymentMethodInfo!!, it.isStored, false,
                  savedInstanceState)
              handleTopUpClick()
            } else if (paymentType == PaymentType.PAYPAL.name) {
              launchPaypal(it.paymentMethodInfo!!)
            }
            loadBonusIntoView()
          }
        }
        .subscribe({}, { handleSpecificError(R.string.unknown_error, it) }))
  }

  private fun launchPaypal(paymentMethodInfo: PaymentMethod) {
    disposables.add(
        adyenPaymentInteractor.makeTopUpPayment(paymentMethodInfo, false, false, emptyList(),
            returnUrl, retrievedAmount, retrievedCurrency,
            mapPaymentToService(paymentType).transactionType, transactionType, appPackage)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .filter { !waitingResult }
            .doOnSuccess { handlePaymentModel(it) }
            .subscribe({}, { handleSpecificError(R.string.unknown_error, it) }))
  }

  //Called if is card
  private fun handleTopUpClick() {
    disposables.add(Observable.merge(view.topUpButtonClicked(), view.billingAddressInput())
        .flatMapSingle {
          view.retrievePaymentData()
              .firstOrError()
        }
        .doOnNext {
          view.showLoading()
          view.lockRotation()
          view.setFinishingPurchase()
        }
        .observeOn(networkScheduler)
        .flatMapSingle {
          val billingAddressModel = view.retrieveBillingAddressData()
          val shouldStore = billingAddressModel?.remember ?: it.shouldStoreCard
          topUpAnalytics.sendConfirmationEvent(appcValue.toDouble(), "top_up", paymentType)
          adyenPaymentInteractor.makeTopUpPayment(it.cardPaymentMethod, shouldStore,
              it.hasCvc, it.supportedShopperInteractions, returnUrl, retrievedAmount,
              retrievedCurrency, mapPaymentToService(paymentType).transactionType,
              transactionType,
              appPackage, mapToAdyenBillingAddress(billingAddressModel))
        }
        .observeOn(viewScheduler)
        .flatMapCompletable {
          if (it.action != null) {
            Completable.fromAction { handlePaymentModel(it) }
          } else {
            handlePaymentResult(it)
          }
        }
        .subscribe({}, {
          view.showSpecificError(R.string.unknown_error)
          logger.log(TAG, it)
        }))
  }

  private fun handleForgetCardClick() {
    disposables.add(view.forgetCardClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showLoading() }
        .observeOn(networkScheduler)
        .flatMapSingle { adyenPaymentInteractor.disablePayments() }
        .observeOn(viewScheduler)
        .doOnNext { success ->
          if (!success) {
            handleSpecificError(R.string.unknown_error, logMessage = "Unable to forget card")
          }
        }
        .filter { it }
        .observeOn(networkScheduler)
        .flatMapSingle {
          adyenPaymentInteractor.loadPaymentInfo(mapPaymentToService(paymentType),
              amount, currency)
              .observeOn(viewScheduler)
              .doOnSuccess {
                view.hideLoading()
                if (it.error.hasError) {
                  if (it.error.isNetworkError) view.showNetworkError()
                  else {
                    handleSpecificError(R.string.unknown_error,
                        logMessage = "Message: ${it.error.message}, code: ${it.error.code}")
                  }
                } else {
                  view.finishCardConfiguration(it.paymentMethodInfo!!, it.isStored, true, null)
                }
              }
        }
        .subscribe({}, { handleSpecificError(R.string.unknown_error, it) }))
  }

  private fun handleRedirectResponse() {
    disposables.add(navigator.uriResults()
        .doOnNext {
          topUpAnalytics.sendPaypalUrlEvent(appcValue.toDouble(), paymentType,
              it.getQueryParameter("type"), it.getQueryParameter("resultCode"), it.toString())
        }
        .observeOn(viewScheduler)
        .doOnNext { view.submitUriResult(it) }
        .subscribe({}, { handleSpecificError(R.string.unknown_error, it) }))
  }

  //Called if is paypal or 3DS
  private fun handlePaymentDetails() {
    disposables.add(view.getPaymentDetails()
        .observeOn(viewScheduler)
        .doOnNext {
          view.lockRotation()
          view.hideKeyboard()
          view.setFinishingPurchase()
        }
        .throttleLast(2, TimeUnit.SECONDS)
        .observeOn(networkScheduler)
        .flatMapSingle {
          adyenPaymentInteractor.submitRedirect(cachedUid, it.details!!,
              it.paymentData ?: cachedPaymentData)
        }
        .observeOn(viewScheduler)
        .flatMapCompletable { handlePaymentResult(it) }
        .subscribe({}, { handleSpecificError(R.string.unknown_error, it) }))
  }

  private fun handlePaymentResult(paymentModel: PaymentModel): Completable {
    return when {
      paymentModel.resultCode.equals("AUTHORISED", ignoreCase = true) -> {
        adyenPaymentInteractor.getAuthorisedTransaction(paymentModel.uid)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .flatMapCompletable {
              if (it.status == TransactionStatus.COMPLETED) {
                handleSuccessTransaction()
              } else {
                if (paymentModel.status == TransactionStatus.FAILED && paymentType == PaymentType.PAYPAL.name) {
                  retrieveFailedReason(paymentModel.uid)
                } else {
                  Completable.fromAction { handleErrors(paymentModel, appcValue.toDouble()) }
                }
              }
            }
      }
      paymentModel.status == TransactionStatus.PENDING_USER_PAYMENT && paymentModel.action != null -> {
        Completable.fromAction {
          view.showLoading()
          view.lockRotation()
          handleAdyenAction(paymentModel)
        }
      }
      paymentModel.refusalReason != null -> Completable.fromAction {
        topUpAnalytics.sendErrorEvent(appcValue.toDouble(), paymentType, "error",
            paymentModel.refusalCode.toString(), paymentModel.refusalReason ?: "")
        paymentModel.refusalCode?.let { code ->
          when (code) {
            CVC_DECLINED -> view.showCvvError()
            FRAUD -> handleFraudFlow(adyenErrorCodeMapper.map(code))
            else -> handleSpecificError(adyenErrorCodeMapper.map(code))
          }
        }
      }
      paymentModel.error.hasError -> Completable.fromAction {
        if (isBillingAddressError(paymentModel.error)) {
          view.navigateToBillingAddress(retrievedAmount, retrievedCurrency)
        } else {
          handleErrors(paymentModel, appcValue.toDouble())
        }
      }
      paymentModel.status == TransactionStatus.CANCELED -> Completable.fromAction {
        topUpAnalytics.sendErrorEvent(appcValue.toDouble(), paymentType, "error", "",
            "canceled")
        view.cancelPayment()
      }
      paymentModel.status == TransactionStatus.FAILED && paymentType == PaymentType.PAYPAL.name -> {
        retrieveFailedReason(paymentModel.uid)
      }
      else -> Completable.fromAction {
        topUpAnalytics.sendErrorEvent(appcValue.toDouble(), paymentType, "error",
            paymentModel.refusalCode.toString(), "${paymentModel.status}: Generic Error")
        handleSpecificError(R.string.unknown_error)
      }
    }
  }

  private fun retrieveFailedReason(uid: String): Completable {
    return adyenPaymentInteractor.getFailedTransactionReason(uid)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .flatMapCompletable {
          Completable.fromAction {
            topUpAnalytics.sendErrorEvent(appcValue.toDouble(), paymentType, "error",
                it.errorCode.toString(), it.errorMessage ?: "")
            val message = if (it.errorCode != null) adyenErrorCodeMapper.map(it.errorCode!!)
            else R.string.unknown_error
            handleSpecificError(message)
          }
        }
  }

  private fun isBillingAddressError(error: Error): Boolean {
    return error.code != null
        && error.code == 400
        && error.message?.contains("payment.billing_address") == true
  }

  private fun handleSuccessTransaction(): Completable {
    return Completable.fromAction {
      topUpAnalytics.sendSuccessEvent(appcValue.toDouble(), paymentType, "success")
      val bundle = createBundle(retrievedAmount, retrievedCurrency, fiatCurrencySymbol)
      waitingResult = false
      navigator.popView(bundle)
    }
  }

  private fun handleFraudFlow(@StringRes error: Int) {
    disposables.add(
        adyenPaymentInteractor.isWalletBlocked()
            .subscribeOn(networkScheduler)
            .observeOn(networkScheduler)
            .flatMap { blocked ->
              if (blocked) {
                adyenPaymentInteractor.isWalletVerified()
                    .observeOn(viewScheduler)
                    .doOnSuccess {
                      if (it) handleSpecificError(error)
                      else view.showWalletValidation(error)
                    }
              } else {
                Single.just(true)
                    .observeOn(viewScheduler)
                    .doOnSuccess { handleSpecificError(error) }
              }
            }
            .observeOn(viewScheduler)
            .subscribe({}, { handleSpecificError(error, it) })
    )
  }

  private fun handleAdyen3DSErrors() {
    disposables.add(view.onAdyen3DSError()
        .observeOn(viewScheduler)
        .doOnNext {
          if (it == CHALLENGE_CANCELED) view.navigateToPaymentSelection()
          else handleSpecificError(R.string.unknown_error, logMessage = it)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun buildRefusalReason(status: TransactionStatus, message: String?): String {
    return message?.let { "$status : $it" } ?: status.toString()
  }

  private fun handlePaymentModel(paymentModel: PaymentModel) {
    if (paymentModel.error.hasError) {
      handleErrors(paymentModel, appcValue.toDouble())
    } else {
      view.showLoading()
      view.lockRotation()
      handleAdyenAction(paymentModel)
    }
  }

  private fun convertAmount(): Single<BigDecimal> {
    return if (selectedCurrency == TopUpData.FIAT_CURRENCY) {
      Single.just(BigDecimal(amount))
    } else adyenPaymentInteractor.convertToLocalFiat(BigDecimal(appcValue).toDouble())
        .map(FiatValue::amount)
  }

  private fun createBundle(priceAmount: String, priceCurrency: String,
                           fiatCurrencySymbol: String): Bundle {
    return billingMessagesMapper.topUpBundle(priceAmount, priceCurrency, bonusValue.toPlainString(),
        fiatCurrencySymbol)
  }

  private fun mapPaymentToService(paymentType: String): AdyenPaymentRepository.Methods {
    return if (paymentType == PaymentType.CARD.name) {
      AdyenPaymentRepository.Methods.CREDIT_CARD
    } else {
      AdyenPaymentRepository.Methods.PAYPAL
    }
  }

  private fun mapToAdyenBillingAddress(
      billingAddressModel: BillingAddressModel?): AdyenBillingAddress? {
    return billingAddressModel?.let {
      AdyenBillingAddress(it.address, it.city, it.zipcode, it.number, it.state, it.country)
    }
  }

  fun stop() = disposables.clear()

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(WAITING_RESULT, waitingResult)
    outState.putInt(CURRENT_ERROR, currentError)
    outState.putString(UID, cachedUid)
    outState.putString(RETRIEVED_AMOUNT, retrievedAmount)
    outState.putString(RETRIEVED_CURRENCY, retrievedCurrency)
    outState.putString(PAYMENT_DATA, cachedPaymentData)
  }

  private fun retrieveSavedInstance(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      waitingResult = it.getBoolean(WAITING_RESULT)
      currentError = it.getInt(CURRENT_ERROR)
      cachedUid = it.getString(UID, "")
      cachedPaymentData = it.getString(PAYMENT_DATA)
      retrievedAmount = it.getString(RETRIEVED_AMOUNT, amount)
      retrievedCurrency = it.getString(RETRIEVED_CURRENCY, currency)
    }
  }

  private fun handleSpecificError(@StringRes message: Int, throwable: Throwable? = null,
                                  logMessage: String? = null) {
    if (throwable != null) logger.log(TAG, throwable)
    if (logMessage != null) logger.log(TAG, logMessage)
    currentError = message
    waitingResult = false
    view.showSpecificError(message)
  }

  private fun hideSpecificError() {
    currentError = 0
    view.hideErrorViews()
  }

  private fun handleAdyenAction(paymentModel: PaymentModel) {
    if (paymentModel.action != null) {
      val type = paymentModel.action?.type
      if (type == REDIRECT) {
        cachedPaymentData = paymentModel.paymentData
        cachedUid = paymentModel.uid
        navigator.navigateToUriForResult(paymentModel.redirectUrl)
        waitingResult = true
      } else if (type == THREEDS2FINGERPRINT || type == THREEDS2CHALLENGE) {
        cachedUid = paymentModel.uid
        view.handle3DSAction(paymentModel.action!!)
        waitingResult = true
      } else {
        handleSpecificError(R.string.unknown_error, logMessage = "Unknown adyen action: $type")
      }
    }
  }

  private fun handleErrors(paymentModel: PaymentModel, value: Double) {
    when {
      paymentModel.error.isNetworkError -> {
        topUpAnalytics.sendErrorEvent(value, paymentType, "error",
            paymentModel.error.code.toString(),
            "network_error")
        view.showNetworkError()
      }
      paymentModel.error.code != null -> {
        topUpAnalytics.sendErrorEvent(value, paymentType, "error",
            paymentModel.error.code.toString(),
            buildRefusalReason(paymentModel.status, paymentModel.error.message))
        val resId = servicesErrorMapper.mapError(paymentModel.error.code!!)
        if (paymentModel.error.code == HTTP_FRAUD_CODE) handleFraudFlow(resId)
        else view.showSpecificError(resId)
      }
      else -> {
        topUpAnalytics.sendErrorEvent(value, paymentType, "error",
            paymentModel.error.code.toString(),
            buildRefusalReason(paymentModel.status, paymentModel.error.message))
        handleSpecificError(R.string.unknown_error)
      }
    }
  }

  companion object {
    private const val WAITING_RESULT = "WAITING_RESULT"
    private const val CURRENT_ERROR = "current_error"
    private const val RETRIEVED_AMOUNT = "RETRIEVED_AMOUNT"
    private const val RETRIEVED_CURRENCY = "RETRIEVED_CURRENCY"
    private const val UID = "UID"
    private const val PAYMENT_DATA = "payment_data"
    private const val HTTP_FRAUD_CODE = 403
    private const val CHALLENGE_CANCELED = "Challenge canceled."
    private val TAG = AdyenTopUpPresenter::class.java.name
  }

}