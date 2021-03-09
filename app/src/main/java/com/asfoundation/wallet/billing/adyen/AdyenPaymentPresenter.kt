package com.asfoundation.wallet.billing.adyen

import android.os.Bundle
import androidx.annotation.StringRes
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.appcoins.wallet.billing.Voucher
import com.appcoins.wallet.billing.adyen.AdyenBillingAddress
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.REDIRECT
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2CHALLENGE
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2FINGERPRINT
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.appcoins.wallet.billing.util.Error
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.FacebookEventLogger
import com.asfoundation.wallet.billing.address.BillingAddressModel
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper.Companion.CVC_DECLINED
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper.Companion.FRAUD
import com.asfoundation.wallet.billing.adyen.AdyenPaymentAnalytics.Event
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor.Companion.PAYMENT_METHOD_CHECK_ID
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class AdyenPaymentPresenter(private val view: AdyenPaymentView,
                            private val disposables: CompositeDisposable,
                            private val viewScheduler: Scheduler,
                            private val networkScheduler: Scheduler,
                            private val data: AdyenPaymentData,
                            private val analytics: AdyenPaymentAnalytics,
                            private val adyenPaymentInteractor: AdyenPaymentInteractor,
                            private val navigator: AdyenPaymentNavigator,
                            private val adyenErrorCodeMapper: AdyenErrorCodeMapper,
                            private val servicesErrorCodeMapper: ServicesErrorCodeMapper,
                            private val formatter: CurrencyFormatUtils,
                            private val logger: Logger) {

  private var waitingResult = false
  private var cachedUid = ""
  private var cachedPaymentData: String? = null

  fun present(savedInstanceState: Bundle?) {
    view.setupUi(data)
    retrieveSavedInstace(savedInstanceState)
    view.setup3DSComponent()
    view.setupRedirectComponent()
    if (!waitingResult) loadPaymentMethodInfo(savedInstanceState)
    handleBack()
    handleErrorDismissEvent()
    handleForgetCardClick()
    handleRedirectResponse()
    handlePaymentDetails()
    handleAdyenErrorBack()
    handleAdyenErrorCancel()
    handleSupportClicks()
    handle3DSErrors()
    if (data.isPreselected) handleMorePaymentsClick()
  }

  private fun handleSupportClicks() {
    disposables.add(
        Observable.merge(view.getAdyenSupportIconClicks(), view.getAdyenSupportLogoClicks())
            .throttleFirst(50, TimeUnit.MILLISECONDS)
            .observeOn(viewScheduler)
            .flatMapCompletable { adyenPaymentInteractor.showSupport(data.gamificationLevel) }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleForgetCardClick() {
    disposables.add(view.forgetCardClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showLoading(data.isPreselected, data.bonus) }
        .observeOn(networkScheduler)
        .flatMapSingle { adyenPaymentInteractor.disablePayments() }
        .observeOn(viewScheduler)
        .doOnNext { success -> if (!success) view.showGenericError() }
        .filter { it }
        .observeOn(networkScheduler)
        .flatMapSingle {
          adyenPaymentInteractor.loadPaymentInfo(mapPaymentToService(data.paymentType),
              data.paymentData.appcAmount.toString(), data.paymentData.currency)
              .observeOn(viewScheduler)
              .doOnSuccess {
                adyenPaymentInteractor.forgetBillingAddress()
                view.hideLoadingAndShowView(data.isPreselected, data.bonus)
                if (it.error.hasError) {
                  if (it.error.isNetworkError) view.showNetworkError()
                  else view.showGenericError()
                } else {
                  view.finishCardConfiguration(it.paymentMethodInfo!!, it.isStored, true, null)
                }
              }
        }
        .subscribe({}, {
          logger.log(TAG, it)
          view.showGenericError()
        }))
  }

  private fun loadPaymentMethodInfo(savedInstanceState: Bundle?) {
    view.showLoading(data.isPreselected, data.bonus)
    disposables.add(
        adyenPaymentInteractor.loadPaymentInfo(mapPaymentToService(data.paymentType),
            data.paymentData.fiatAmount.toString(), data.paymentData.currency)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              if (it.error.hasError) {
                sendPaymentErrorEvent(it.error.code, it.error.message)
                view.hideLoadingAndShowView(data.isPreselected, data.bonus)
                handleErrors(it.error)
              } else {
                val amount = formatter.formatCurrency(it.priceAmount, WalletCurrency.FIAT)
                view.showProductPrice(amount, it.priceCurrency)
                if (data.paymentType == PaymentType.CARD.name) {
                  view.hideLoadingAndShowView(data.isPreselected, data.bonus)
                  sendPaymentMethodDetailsEvent()
                  view.finishCardConfiguration(it.paymentMethodInfo!!, it.isStored, false,
                      savedInstanceState)
                  handleBuyClick(it.priceAmount, it.priceCurrency)
                } else if (data.paymentType == PaymentType.PAYPAL.name) {
                  launchPaypal(it.paymentMethodInfo!!, it.priceAmount, it.priceCurrency)
                }
              }
            }
            .subscribe({}, {
              logger.log(TAG, it)
              view.showGenericError()
            }))
  }

  private fun launchPaypal(paymentMethodInfo: PaymentMethod, priceAmount: BigDecimal,
                           priceCurrency: String) {
    val paymentData = data.paymentData
    disposables.add(
        adyenPaymentInteractor.makePayment(paymentMethodInfo, false, false, emptyList(),
            data.returnUrl, priceAmount.toString(), priceCurrency, paymentData.orderReference,
            mapPaymentToService(data.paymentType).transactionType, paymentData.origin,
            paymentData.domain, paymentData.payload, paymentData.skuId, paymentData.callbackUrl,
            paymentData.type, paymentData.toAddress, paymentData.referrerUrl)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .filter { !waitingResult }
            .doOnSuccess {
              view.hideLoadingAndShowView(data.isPreselected, data.bonus)
              handlePaymentModel(it)
            }
            .subscribe({}, {
              logger.log(TAG, it)
              view.showGenericError()
            }))
  }

  private fun handlePaymentModel(paymentModel: PaymentModel) {
    if (paymentModel.error.hasError) {
      handleErrors(paymentModel.error)
    } else {
      view.showLoading(data.isPreselected, data.bonus)
      view.lockRotation()
      sendPaymentMethodDetailsEvent()
      handleAdyenAction(paymentModel)
    }
  }

  private fun handleBuyClick(priceAmount: BigDecimal, priceCurrency: String) {
    disposables.add(Observable.merge(view.buyButtonClicked(), view.billingAddressInput())
        .flatMapSingle {
          view.retrievePaymentData()
              .firstOrError()
        }
        .observeOn(viewScheduler)
        .doOnNext {
          view.showLoading(data.isPreselected, data.bonus)
          view.hideKeyboard()
          view.lockRotation()
        }
        .observeOn(networkScheduler)
        .flatMapSingle { adyenCard ->
          handleBuyAnalytics()
          val billingAddressModel = view.retrieveBillingAddressData()
          val shouldStore = billingAddressModel?.remember ?: adyenCard.shouldStoreCard
          val paymentData = data.paymentData
          adyenPaymentInteractor.makePayment(adyenCard.cardPaymentMethod,
              shouldStore, adyenCard.hasCvc, adyenCard.supportedShopperInteractions,
              data.returnUrl, priceAmount.toString(), priceCurrency, paymentData.orderReference,
              mapPaymentToService(data.paymentType).transactionType, paymentData.origin,
              paymentData.domain, paymentData.payload, paymentData.skuId, paymentData.callbackUrl,
              paymentData.type, paymentData.toAddress, paymentData.referrerUrl,
              mapToAdyenBillingAddress(billingAddressModel))
        }
        .observeOn(viewScheduler)
        .flatMapCompletable { handlePaymentResult(it, priceAmount, priceCurrency) }
        .subscribe({}, {
          logger.log(TAG, it)
          view.showGenericError()
        }))
  }

  private fun handlePaymentResult(paymentModel: PaymentModel, priceAmount: BigDecimal? = null,
                                  priceCurrency: String? = null): Completable {
    return when {
      paymentModel.resultCode.equals("AUTHORISED", true) -> {
        adyenPaymentInteractor.getAuthorisedTransaction(paymentModel.uid)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .flatMapCompletable {
              when {
                it.status == TransactionStatus.COMPLETED -> {
                  createBundle(it.hash, it.orderReference)
                      .doOnSuccess {
                        sendEvent(Event.SUCCESS)
                        sendEvent(Event.PAYMENT_EVENT)
                        sendRevenueEvent()
                      }
                      .subscribeOn(networkScheduler)
                      .observeOn(viewScheduler)
                      .flatMapCompletable { bundle ->
                        handleSuccessTransaction(bundle, paymentModel.voucher)
                      }
                }
                isPaymentFailed(it.status) -> {
                  if (paymentModel.status == TransactionStatus.FAILED && data.paymentType == PaymentType.PAYPAL.name) {
                    retrieveFailedReason(paymentModel.uid)
                  } else {
                    Completable.fromAction {
                      sendPaymentErrorEvent(it.error.code,
                          buildRefusalReason(it.status, it.error.message))
                      handleErrors(it.error)
                    }
                        .subscribeOn(viewScheduler)
                  }
                }
                else -> {
                  sendPaymentErrorEvent(it.error.code, it.status.toString())
                  Completable.fromAction { handleErrors(it.error) }
                }
              }
            }
      }
      paymentModel.status == TransactionStatus.PENDING_USER_PAYMENT && paymentModel.action != null -> {
        Completable.fromAction {
          view.showLoading(data.isPreselected, data.bonus)
          view.lockRotation()
          handleAdyenAction(paymentModel)
        }
      }
      paymentModel.refusalReason != null -> Completable.fromAction {
        var riskRules: String? = null
        paymentModel.refusalCode?.let { code ->
          when (code) {
            CVC_DECLINED -> view.showCvvError(data.isPreselected, data.bonus)
            FRAUD -> {
              handleFraudFlow(adyenErrorCodeMapper.map(code), paymentModel.fraudResultIds)
              riskRules = paymentModel.fraudResultIds.sorted()
                  .joinToString(separator = "-")
            }
            else -> view.showSpecificError(adyenErrorCodeMapper.map(code))
          }
        }
        sendPaymentErrorEvent(paymentModel.refusalCode, paymentModel.refusalReason, riskRules)
      }
      paymentModel.error.hasError -> Completable.fromAction {
        if (isBillingAddressError(paymentModel.error, priceAmount, priceCurrency)) {
          view.showBillingAddress(priceAmount!!, priceCurrency!!, data.bonus,
              data.paymentData.appcAmount)
        } else {
          sendPaymentErrorEvent(paymentModel.error.code, paymentModel.error.message)
          handleErrors(paymentModel.error)
        }
      }
      paymentModel.status == TransactionStatus.FAILED && data.paymentType == PaymentType.PAYPAL.name -> {
        retrieveFailedReason(paymentModel.uid)
      }
      paymentModel.status == TransactionStatus.CANCELED -> Completable.fromAction { view.showMoreMethods() }
      else -> Completable.fromAction {
        sendPaymentErrorEvent(paymentModel.error.code, "${paymentModel.status}: Generic Error")
        view.showGenericError()
      }
    }
  }

  private fun isBillingAddressError(error: Error,
                                    priceAmount: BigDecimal?,
                                    priceCurrency: String?): Boolean {
    return error.code != null
        && error.code == 400
        && error.message?.contains("payment.billing_address") == true
        && priceAmount != null
        && priceCurrency != null
  }

  private fun handleSuccessTransaction(bundle: Bundle,
                                       voucher: Voucher?): Completable {
    return if (voucher != null) {
      Completable.fromAction {
        navigator.navigateToVoucherSuccess(data.bonus, voucher.code, voucher.redeem)
      }
    } else {
      Completable.fromAction { view.showSuccess(data.isPreselected) }
          .andThen(Completable.timer(view.getAnimationDuration(),
              TimeUnit.MILLISECONDS))
          .andThen(Completable.fromAction { navigator.finishPayment(bundle) })
    }
  }

  private fun retrieveFailedReason(uid: String): Completable {
    return adyenPaymentInteractor.getFailedTransactionReason(uid)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .flatMapCompletable {
          Completable.fromAction {
            sendPaymentErrorEvent(it.errorCode, it.errorMessage ?: "")
            if (it.errorCode != null) view.showSpecificError(
                adyenErrorCodeMapper.map(it.errorCode!!))
            else view.showGenericError()
          }
        }
  }

  private fun handleFraudFlow(@StringRes error: Int, fraudCheckIds: List<Int>) {
    disposables.add(
        adyenPaymentInteractor.isWalletBlocked()
            .subscribeOn(networkScheduler)
            .observeOn(networkScheduler)
            .flatMap { blocked ->
              if (blocked) {
                adyenPaymentInteractor.isWalletVerified()
                    .observeOn(viewScheduler)
                    .doOnSuccess {
                      if (it) view.showSpecificError(error)
                      else view.showVerification()
                    }
              } else {
                Single.just(fraudCheckIds)
                    .observeOn(viewScheduler)
                    .doOnSuccess {
                      val fraudError = when {
                        it.contains(PAYMENT_METHOD_CHECK_ID) -> {
                          R.string.purchase_error_try_other_method
                        }
                        else -> error
                      }
                      view.showSpecificError(fraudError)
                    }
              }
            }
            .observeOn(viewScheduler)
            .subscribe({}, {
              view.showSpecificError(error)
              logger.log(TAG, it)
            })
    )
  }

  private fun buildRefusalReason(status: TransactionStatus, message: String?): String {
    return message?.let { "$status : $it" } ?: status.toString()
  }

  private fun isPaymentFailed(status: TransactionStatus): Boolean {
    return status == TransactionStatus.FAILED || status == TransactionStatus.CANCELED || status == TransactionStatus.INVALID_TRANSACTION
  }

  private fun handlePaymentDetails() {
    disposables.add(view.getPaymentDetails()
        .throttleLast(2, TimeUnit.SECONDS)
        .observeOn(viewScheduler)
        .doOnNext { view.lockRotation() }
        .observeOn(networkScheduler)
        .flatMapSingle {
          adyenPaymentInteractor.submitRedirect(cachedUid, it.details!!,
              it.paymentData ?: cachedPaymentData)
        }
        .observeOn(viewScheduler)
        .flatMapCompletable { handlePaymentResult(it) }
        .subscribe({}, {
          logger.log(TAG, it)
          view.showGenericError()
        }))
  }

  private fun handle3DSErrors() {
    disposables.add(view.onAdyen3DSError()
        .observeOn(viewScheduler)
        .doOnNext {
          if (it == CHALLENGE_CANCELED) view.showMoreMethods()
          else {
            logger.log(TAG, it)
            view.showGenericError()
          }
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(WAITING_RESULT, waitingResult)
    outState.putString(UID, cachedUid)
    outState.putString(PAYMENT_DATA, cachedPaymentData)
  }

  private fun retrieveSavedInstace(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      waitingResult = it.getBoolean(WAITING_RESULT)
      cachedUid = it.getString(UID, "")
      cachedPaymentData = it.getString(PAYMENT_DATA)
    }
  }

  private fun sendPaymentMethodDetailsEvent() = sendEvent(Event.PAYMENT_DETAILS)

  private fun handleErrorDismissEvent() {
    disposables.add(view.errorDismisses()
        .observeOn(viewScheduler)
        .doOnNext { navigator.finishPaymentWithError() }
        .subscribe({}, { navigator.finishPaymentWithError() }))
  }

  private fun handleBack() {
    disposables.add(view.backEvent()
        .observeOn(networkScheduler)
        .doOnNext { handlePaymentMethodAnalytics() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handlePaymentMethodAnalytics() {
    if (data.isPreselected) {
      sendEvent(Event.PRE_SELECTED_PAYMENT_METHOD, WalletsAnalytics.ACTION_CANCEL)
      view.close(adyenPaymentInteractor.mapCancellation())
    } else {
      sendEvent(Event.CONFIRMATION, WalletsAnalytics.ACTION_BACK)
      view.showMoreMethods()
    }
  }

  private fun handleMorePaymentsClick() {
    disposables.add(view.getMorePaymentMethodsClicks()
        .observeOn(viewScheduler)
        .doOnNext { showMoreMethods() }
        .observeOn(networkScheduler)
        .doOnNext {
          sendEvent(Event.PRE_SELECTED_PAYMENT_METHOD, WalletsAnalytics.ACTION_OTHER_PAYMENT)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleRedirectResponse() {
    disposables.add(navigator.uriResults()
        .observeOn(viewScheduler)
        .doOnNext { view.submitUriResult(it) }
        .subscribe({}, {
          logger.log(TAG, it)
          view.showGenericError()
        }))
  }

  private fun showMoreMethods() {
    adyenPaymentInteractor.removePreSelectedPaymentMethod()
    view.showMoreMethods()
  }

  private fun sendRevenueEvent() {
    disposables.add(adyenPaymentInteractor.convertToFiat(data.paymentData.appcAmount
        .toDouble(), FacebookEventLogger.EVENT_REVENUE_CURRENCY)
        .subscribeOn(networkScheduler)
        .doOnSuccess { sendEvent(Event.REVENUE) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun sendPaymentErrorEvent(refusalCode: Int?, refusalReason: String?,
                                    riskRules: String? = null) {
    sendEvent(Event.ADYEN_ERROR_AND_RISK_RULES, null, refusalCode.toString(), refusalReason,
        riskRules)
  }

  private fun mapToAdyenBillingAddress(
      billingAddressModel: BillingAddressModel?): AdyenBillingAddress? {
    return billingAddressModel?.let {
      AdyenBillingAddress(it.address, it.city, it.zipcode, it.number, it.state, it.country)
    }
  }

  private fun createBundle(hash: String?, orderReference: String?): Single<Bundle> {
    val paymentData = data.paymentData
    return adyenPaymentInteractor.getCompletePurchaseBundle(paymentData.type, paymentData.domain,
        paymentData.skuId, orderReference, hash, networkScheduler)
        .map { mapPaymentMethodId(it) }
  }

  private fun mapPaymentMethodId(bundle: Bundle): Bundle {
    if (data.paymentType == PaymentType.CARD.name) {
      bundle.putString(InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
          PaymentMethodsView.PaymentMethodId.CREDIT_CARD.id)
    } else if (data.paymentType == PaymentType.PAYPAL.name) {
      bundle.putString(InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
          PaymentMethodsView.PaymentMethodId.PAYPAL.id)
    }
    return bundle
  }

  private fun handleBuyAnalytics() {
    if (data.isPreselected) {
      sendEvent(Event.PRE_SELECTED_PAYMENT_METHOD, WalletsAnalytics.ACTION_BUY)
    } else {
      sendEvent(Event.CONFIRMATION, WalletsAnalytics.ACTION_BUY)
    }
  }

  private fun handleAdyenErrorBack() {
    disposables.add(view.adyenErrorBackClicks()
        .observeOn(viewScheduler)
        .doOnNext {
          if (data.isPreselected) {
            view.close(adyenPaymentInteractor.mapCancellation())
          } else {
            view.showMoreMethods()
          }
        }
        .subscribe({}, {
          logger.log(TAG, it)
          view.showGenericError()
        }
        ))
  }

  private fun handleAdyenErrorCancel() {
    disposables.add(view.adyenErrorCancelClicks()
        .observeOn(viewScheduler)
        .doOnNext { view.close(adyenPaymentInteractor.mapCancellation()) }
        .subscribe({}, {
          logger.log(TAG, it)
          view.showGenericError()
        }))
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
        logger.log(TAG, "Unknown adyen action: $type")
        view.showGenericError()
      }
    }
  }

  private fun mapPaymentToService(paymentType: String): AdyenPaymentRepository.Methods {
    return if (paymentType == PaymentType.CARD.name) {
      AdyenPaymentRepository.Methods.CREDIT_CARD
    } else {
      AdyenPaymentRepository.Methods.PAYPAL
    }
  }

  private fun sendEvent(event: Event, action: String? = null, refusalCode: String? = null,
                        refusalReason: String? = null, riskRules: String? = null) {
    val paymentData = data.paymentData
    analytics.sendAnalytics(paymentData.domain, paymentData.skuId,
        paymentData.appcAmount.toString(), data.paymentType, paymentData.type, action, refusalCode,
        refusalReason, riskRules, event)
  }

  fun stop() = disposables.clear()

  companion object {

    private const val WAITING_RESULT = "WAITING_RESULT"
    private const val HTTP_FRAUD_CODE = 403
    private const val UID = "UID"
    private const val PAYMENT_DATA = "payment_data"
    private const val CHALLENGE_CANCELED = "Challenge canceled."
    private val TAG = AdyenPaymentPresenter::class.java.name
  }

  private fun handleErrors(error: Error) {
    when {
      error.isNetworkError -> view.showNetworkError()
      error.code != null -> {
        val resId = servicesErrorCodeMapper.mapError(error.code!!)
        if (error.code == HTTP_FRAUD_CODE) handleFraudFlow(resId, emptyList())
        else view.showSpecificError(resId)
      }
      else -> view.showGenericError()
    }
  }
}
