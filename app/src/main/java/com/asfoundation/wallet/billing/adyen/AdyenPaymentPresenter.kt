package com.asfoundation.wallet.billing.adyen

import android.os.Bundle
import androidx.annotation.StringRes
import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.ErrorInfo.ErrorType
import com.appcoins.wallet.core.network.microservices.model.AdyenBillingAddress
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.REDIRECT
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2CHALLENGE
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2FINGERPRINT
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.*
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asf.wallet.R
import com.asfoundation.wallet.billing.address.BillingAddressModel
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper.Companion.CVC_DECLINED
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper.Companion.FRAUD
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asfoundation.wallet.ui.iab.*
import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class AdyenPaymentPresenter(
  private val view: AdyenPaymentView,
  private val iabView: IabView,
  private val disposables: CompositeDisposable,
  private val viewScheduler: Scheduler,
  private val networkScheduler: Scheduler,
  private val returnUrl: String,
  private val analytics: BillingAnalytics,
  private val paymentAnalytics: PaymentMethodsAnalytics,
  private val origin: String?,
  private val adyenPaymentInteractor: AdyenPaymentInteractor,
  private val skillsPaymentInteractor: SkillsPaymentInteractor,
  private val transactionBuilder: TransactionBuilder,
  private val navigator: Navigator,
  private val paymentType: String,
  private val amount: BigDecimal,
  private val currency: String,
  private val skills: Boolean,
  private val isPreSelected: Boolean,
  private val adyenErrorCodeMapper: AdyenErrorCodeMapper,
  private val servicesErrorCodeMapper: ServicesErrorCodeMapper,
  private val gamificationLevel: Int,
  private val formatter: CurrencyFormatUtils,
  private val logger: Logger
) {

  private var waitingResult = false
  private var cachedUid = ""
  private var cachedPaymentData: String? = null
  private var action3ds: String? = null

  fun present(savedInstanceState: Bundle?) {
    retrieveSavedInstace(savedInstanceState)
    view.setup3DSComponent()
    view.setupRedirectComponent()
    if (!waitingResult) loadPaymentMethodInfo()
    handleBack()
    handleErrorDismissEvent()
    handleForgetCardClick()
    handleRedirectResponse()
    handlePaymentDetails()
    handleAdyenErrorBack()
    handleAdyenErrorBackToCard()
    handleAdyenErrorCancel()
    handleSupportClicks()
    handle3DSErrors()
    handleVerificationClick()
    if (isPreSelected) handleMorePaymentsClick()
  }

  private fun handleSupportClicks() {
    disposables.add(Observable.merge(
      view.getAdyenSupportIconClicks(),
      view.getAdyenSupportLogoClicks()
    )
      .throttleFirst(50, TimeUnit.MILLISECONDS)
      .observeOn(viewScheduler)
      .flatMapCompletable { adyenPaymentInteractor.showSupport(gamificationLevel) }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleForgetCardClick() {
    disposables.add(view.forgetCardClick()
      .observeOn(viewScheduler)
      .doOnNext { view.showLoading() }
      .observeOn(networkScheduler)
      .flatMapSingle { adyenPaymentInteractor.disablePayments() }
      .observeOn(viewScheduler)
      .doOnNext { success -> if (!success) view.showGenericError() }
      .filter { it }
      .observeOn(networkScheduler)
      .flatMapSingle {
        adyenPaymentInteractor.loadPaymentInfo(
          mapPaymentToService(paymentType),
          amount.toString(),
          currency
        )
          .observeOn(viewScheduler)
          .doOnSuccess {
            adyenPaymentInteractor.forgetBillingAddress()
            view.hideLoadingAndShowView()
            if (it.error.hasError) {
              if (it.error.isNetworkError) view.showNetworkError()
              else view.showGenericError()
            } else {
              view.finishCardConfiguration(it, true)
            }
          }
      }
      .subscribe({}, {
        logger.log(TAG, it)
        view.showGenericError()
      })
    )
  }

  var priceAmount: BigDecimal? = null
  var priceCurrency: String? = null
  private fun loadPaymentMethodInfo() {
    view.showLoading()
    disposables.add(adyenPaymentInteractor.loadPaymentInfo(
      mapPaymentToService(paymentType),
      amount.toString(),
      currency
    )
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .doOnSuccess {
        if (it.error.hasError) {
          sendPaymentErrorEvent(it.error.errorInfo?.httpCode, it.error.errorInfo?.text)
          view.hideLoadingAndShowView()
          handleErrors(it.error)
        } else {
          val amount = formatter.formatPaymentCurrency(it.priceAmount, WalletCurrency.FIAT)
          view.showProductPrice(amount, it.priceCurrency)
          when (paymentType) {
            PaymentType.CARD.name -> {
              priceAmount = it.priceAmount
              priceCurrency = it.priceCurrency
              view.hideLoadingAndShowView()
              sendPaymentMethodDetailsEvent(PaymentMethodsAnalytics.PAYMENT_METHOD_CC)
              view.finishCardConfiguration(it, false)
              handleBuyClick(it.priceAmount, it.priceCurrency)
              paymentAnalytics.stopTimingForTotalEvent(PaymentMethodsAnalytics.PAYMENT_METHOD_CC)
            }
            PaymentType.GIROPAY.name -> {
              launchPaymentAdyen(it.paymentMethod!!, it.priceAmount, it.priceCurrency)
            }
            PaymentType.PAYPAL.name -> {
              launchPaymentAdyen(it.paymentMethod!!, it.priceAmount, it.priceCurrency)
            }
          }
        }
      }
      .subscribe({}, {
        logger.log(TAG, it)
        view.showGenericError()
      })
    )
  }

  private fun launchPaymentAdyen(
    paymentMethodInfo: ModelObject,
    priceAmount: BigDecimal,
    priceCurrency: String
  ) {
    disposables.add(
      adyenPaymentInteractor.makePayment(
        adyenPaymentMethod = paymentMethodInfo,
        shouldStoreMethod = false,
        hasCvc = false,
        supportedShopperInteraction = emptyList(),
        returnUrl = returnUrl,
        value = priceAmount.toString(),
        currency = priceCurrency,
        reference = transactionBuilder.orderReference,
        paymentType = mapPaymentToService(paymentType).transactionType,
        origin = origin,
        packageName = transactionBuilder.domain,
        metadata = transactionBuilder.payload,
        sku = transactionBuilder.skuId,
        callbackUrl = transactionBuilder.callbackUrl,
        transactionType = transactionBuilder.type,
        developerWallet = transactionBuilder.toAddress(),
        referrerUrl = transactionBuilder.referrerUrl
      )
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .filter { !waitingResult }
        .doOnSuccess {
          view.hideLoadingAndShowView()
          handlePaymentModel(it)
        }
        .subscribe({}, {
          logger.log(TAG, it)
          view.showGenericError()
        })
    )
  }

  private fun handlePaymentModel(paymentModel: PaymentModel) {
    if (paymentModel.error.hasError) {
      handleErrors(paymentModel.error, paymentModel.refusalCode)
    } else {
      view.showLoading()
      view.lockRotation()
      sendPaymentMethodDetailsEvent(mapPaymentToAnalytics(paymentType))
      paymentAnalytics.stopTimingForTotalEvent(mapPaymentToAnalytics(paymentType))
      paymentAnalytics.startTimingForPurchaseEvent()
      handleAdyenAction(paymentModel)
    }
  }

  private fun handleBuyClick(priceAmount: BigDecimal, priceCurrency: String) {
    disposables.add(Observable.merge(view.buyButtonClicked(), view.billingAddressInput())
      .flatMapSingle {
        paymentAnalytics.startTimingForPurchaseEvent()
        view.retrievePaymentData()
          .firstOrError()
      }
      .observeOn(viewScheduler)
      .doOnNext {
        view.showLoading()
        view.hideKeyboard()
        view.lockRotation()
      }
      .observeOn(networkScheduler)
      .flatMapSingle { adyenCard ->
        handleBuyAnalytics(transactionBuilder)
        val billingAddressModel = view.retrieveBillingAddressData()
        val shouldStore = billingAddressModel?.remember ?: adyenCard.shouldStoreCard
        if (skills) {
          skillsPaymentInteractor.makeSkillsPayment(
            returnUrl,
            transactionBuilder.productToken,
            adyenCard.cardPaymentMethod.encryptedCardNumber,
            adyenCard.cardPaymentMethod.encryptedExpiryMonth,
            adyenCard.cardPaymentMethod.encryptedExpiryYear,
            adyenCard.cardPaymentMethod.encryptedSecurityCode!!
          )
        } else {
          adyenPaymentInteractor.makePayment(
            adyenPaymentMethod = adyenCard.cardPaymentMethod,
            shouldStoreMethod = shouldStore,
            hasCvc = adyenCard.hasCvc,
            supportedShopperInteraction = adyenCard.supportedShopperInteractions,
            returnUrl = returnUrl,
            value = priceAmount.toString(),
            currency = priceCurrency,
            reference = transactionBuilder.orderReference,
            paymentType = mapPaymentToService(paymentType).transactionType,
            origin = origin,
            packageName = transactionBuilder.domain,
            metadata = transactionBuilder.payload,
            sku = transactionBuilder.skuId,
            callbackUrl = transactionBuilder.callbackUrl,
            transactionType = transactionBuilder.type,
            developerWallet = transactionBuilder.toAddress(),
            referrerUrl = transactionBuilder.referrerUrl,
            billingAddress = mapToAdyenBillingAddress(billingAddressModel)
          )
        }
      }
      .observeOn(viewScheduler)
      .flatMapCompletable { handlePaymentResult(it, priceAmount, priceCurrency) }
      .subscribe({}, {
        logger.log(TAG, it)
        view.showGenericError()
      })
    )
  }

  private fun handlePaymentResult(
    paymentModel: PaymentModel,
    priceAmount: BigDecimal? = null,
    priceCurrency: String? = null
  ): Completable = when {
    paymentModel.resultCode.equals("AUTHORISED", true) -> {
      adyenPaymentInteractor.getAuthorisedTransaction(paymentModel.uid)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .flatMapCompletable {
          when {
            it.status == COMPLETED -> {
              sendPaymentSuccessEvent(it.uid)
              createBundle(it.hash, it.orderReference, it.purchaseUid)
                .doOnSuccess {
                  sendPaymentEvent()
                  sendRevenueEvent()
                }
                .subscribeOn(networkScheduler)
                .observeOn(viewScheduler)
                .flatMapCompletable { bundle -> handleSuccessTransaction(bundle) }
            }
            isPaymentFailed(it.status) -> {
              if (paymentModel.status == FAILED && paymentType == PaymentType.PAYPAL.name) {
                retrieveFailedReason(paymentModel.uid)
              } else {
                Completable.fromAction {
                  var errorDetails = buildRefusalReason(it.status, it.error.errorInfo?.text)
                  if (errorDetails.isBlank()) {
                    errorDetails = getWebViewResultCode()
                  }
                  sendPaymentErrorEvent(
                    it.error.errorInfo?.httpCode,
                    errorDetails
                  )
                  handleErrors(it.error, paymentModel.refusalCode)
                }
                  .subscribeOn(viewScheduler)
              }
            }
            else -> {
              var errorDetails = it.status.toString() + it.error.errorInfo?.text
              if (errorDetails.isBlank()) {
                errorDetails = getWebViewResultCode()
              }
              sendPaymentErrorEvent(
                it.error.errorInfo?.httpCode,
                errorDetails
              )
              Completable.fromAction { handleErrors(it.error, it.refusalCode) }
            }
          }
        }
    }
    paymentModel.status == PENDING_USER_PAYMENT && paymentModel.action != null -> {
      Completable.fromAction {
        view.showLoading()
        view.lockRotation()
        handleAdyenAction(paymentModel)
      }
    }
    paymentModel.refusalReason != null -> Completable.fromAction {
      var riskRules: String? = null
      paymentModel.refusalCode?.let { code ->
        when (code) {
          CVC_DECLINED -> view.showCvvError()
          FRAUD -> {
            handleFraudFlow(adyenErrorCodeMapper.map(code), paymentModel.fraudResultIds)
            riskRules = paymentModel.fraudResultIds.sorted()
              .joinToString(separator = "-")
          }
          else -> handleErrors(paymentModel.error, code)
        }
      }
      var errorDetails = paymentModel.refusalReason
      if (errorDetails.isNullOrBlank()) {
        errorDetails = getWebViewResultCode()
      }
      sendPaymentErrorEvent(paymentModel.refusalCode, errorDetails, riskRules)
    }
    paymentModel.error.hasError -> Completable.fromAction {
      if (isBillingAddressError(paymentModel.error, priceAmount, priceCurrency)) {
        view.showBillingAddress(priceAmount!!, priceCurrency!!)
      } else {
        var errorDetails = paymentModel.error.errorInfo?.text
        if (errorDetails.isNullOrBlank()) {
          errorDetails = getWebViewResultCode()
        }
        sendPaymentErrorEvent(
          paymentModel.error.errorInfo?.httpCode,
          errorDetails
        )
        handleErrors(paymentModel.error, paymentModel.refusalCode)
      }
    }
    paymentModel.status == FAILED && paymentType == PaymentType.PAYPAL.name -> {
      retrieveFailedReason(paymentModel.uid)
    }
    paymentModel.status == CANCELED -> Completable.fromAction { view.showMoreMethods() }
    else -> Completable.fromAction {
      var errorDetails = "${paymentModel.status} ${paymentModel.error.errorInfo?.text}"
      if (errorDetails.isBlank()) {
        errorDetails = getWebViewResultCode()
      }
      sendPaymentErrorEvent(
        paymentModel.error.errorInfo?.httpCode,
        errorDetails
      )
      view.showGenericError()
    }
  }

  private fun getWebViewResultCode(): String {
    return "webView Result: ${iabView.webViewResultCode}" ?: ""
  }

  private fun isBillingAddressError(
    error: Error,
    priceAmount: BigDecimal?,
    priceCurrency: String?
  ): Boolean =
    error.errorInfo?.errorType == ErrorType.BILLING_ADDRESS && priceAmount != null && priceCurrency != null

  private fun handleSuccessTransaction(purchaseBundleModel: PurchaseBundleModel): Completable =
    Completable.fromAction { view.showSuccess(purchaseBundleModel.renewal) }
      .andThen(Completable.timer(view.getAnimationDuration(), TimeUnit.MILLISECONDS, viewScheduler))
      .andThen(Completable.fromAction { navigator.popView(purchaseBundleModel.bundle) })

  private fun retrieveFailedReason(uid: String): Completable =
    adyenPaymentInteractor.getFailedTransactionReason(uid)
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .flatMapCompletable {
        Completable.fromAction {
          sendPaymentErrorEvent(it.errorCode, it.errorMessage ?: "")
          if (it.errorCode != null) {
            view.showSpecificError(adyenErrorCodeMapper.map(it.errorCode!!))
          } else {
            view.showGenericError()
          }
        }
      }

  @Suppress("UNUSED_PARAMETER")
  private fun handleFraudFlow(@StringRes error: Int, fraudCheckIds: List<Int>) {
    disposables.add(adyenPaymentInteractor.isWalletVerified()
      .observeOn(viewScheduler)
      .doOnSuccess { verified ->
        view.showVerificationError(verified)
      }
      .subscribe({}, { view.showSpecificError(error) })
    )
  }

  private fun handleVerificationClick() {
    disposables.add(view.getVerificationClicks()
      .throttleFirst(50, TimeUnit.MILLISECONDS)
      .observeOn(viewScheduler)
      .doOnNext { isWalletVerified -> view.showVerification(isWalletVerified) }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun buildRefusalReason(status: PaymentModel.Status, message: String?): String =
    message?.let { "$status : $it" } ?: status.toString()

  private fun isPaymentFailed(status: PaymentModel.Status): Boolean =
    status == FAILED || status == CANCELED || status == INVALID_TRANSACTION || status == PaymentModel.Status.FRAUD

  private fun handlePaymentDetails() {
    disposables.add(view.getPaymentDetails()
      .throttleLast(2, TimeUnit.SECONDS)
      .observeOn(viewScheduler)
      .doOnNext { view.lockRotation() }
      .observeOn(networkScheduler)
      .flatMapSingle {
        adyenPaymentInteractor.submitRedirect(
          uid = cachedUid,
          details = convertToJson(it.details!!),
          paymentData = it.paymentData ?: cachedPaymentData
        )
      }
      .observeOn(viewScheduler)
      .flatMapCompletable { handlePaymentResult(it) }
      .subscribe({}, {
        logger.log(TAG, it)
        view.showGenericError()
      })
    )
  }

  //This method is used to avoid the nameValuePairs key problem that occurs when we pass a JSONObject trough a GSON converter
  private fun convertToJson(details: JSONObject): JsonObject {
    val json = JsonObject()
    val keys = details.keys()
    while (keys.hasNext()) {
      val key = keys.next()
      val value = details.get(key)
      if (value is String) json.addProperty(key, value)
    }
    return json
  }

  private fun handle3DSErrors() {
    disposables.add(view.onAdyen3DSError()
      .observeOn(viewScheduler)
      .doOnNext {
        if (it == CHALLENGE_CANCELED) {
          paymentAnalytics.send3dsCancel()
          view.showMoreMethods()
        } else {
          paymentAnalytics.send3dsError(it)
          logger.log(TAG, "error:$it \n last 3ds action: ${action3ds ?: ""}")
          view.showGenericError()
        }
      }
      .subscribe({}, { it.printStackTrace() })
    )
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

  private fun sendPaymentMethodDetailsEvent(paymentMethod: String) {
    disposables.add(Single.just(transactionBuilder)
      .observeOn(networkScheduler)
      .doOnSuccess { transactionBuilder ->
        analytics.sendPaymentMethodDetailsEvent(
          transactionBuilder.domain,
          transactionBuilder.skuId,
          transactionBuilder.amount().toString(),
          paymentMethod,
          transactionBuilder.type
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleErrorDismissEvent() {
    disposables.add(view.errorDismisses()
      .observeOn(viewScheduler)
      .doOnNext { navigator.popViewWithError() }
      .subscribe({}, { navigator.popViewWithError() })
    )
  }

  private fun handleBack() {
    disposables.add(view.backEvent()
      .observeOn(networkScheduler)
      .doOnNext { handlePaymentMethodAnalytics(transactionBuilder) }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handlePaymentMethodAnalytics(transaction: TransactionBuilder) {
    if (isPreSelected) {
      analytics.sendPreSelectedPaymentMethodEvent(
        transactionBuilder.domain,
        transaction.skuId,
        transaction.amount().toString(),
        mapPaymentToService(paymentType).transactionType,
        transaction.type,
        BillingAnalytics.ACTION_CANCEL
      )
      view.close(adyenPaymentInteractor.mapCancellation())
    } else {
      analytics.sendPaymentConfirmationEvent(
        transactionBuilder.domain,
        transaction.skuId,
        transaction.amount().toString(),
        mapPaymentToService(paymentType).transactionType,
        transaction.type,
        BillingAnalytics.ACTION_BACK
      )
      view.showMoreMethods()
    }
  }

  private fun handleMorePaymentsClick() {
    disposables.add(view.getMorePaymentMethodsClicks()
      .observeOn(networkScheduler)
      .doOnNext { handleMorePaymentsAnalytics(transactionBuilder) }
      .observeOn(viewScheduler)
      .doOnNext { showMoreMethods() }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleMorePaymentsAnalytics(transaction: TransactionBuilder): Unit =
    analytics.sendPreSelectedPaymentMethodEvent(
      transactionBuilder.domain,
      transaction.skuId,
      transaction.amount().toString(),
      mapPaymentToService(paymentType).transactionType,
      transaction.type,
      "other_payments"
    )

  private fun handleRedirectResponse() {
    disposables.add(navigator.uriResults()
      .observeOn(viewScheduler)
      .doOnNext { view.submitUriResult(it) }
      .subscribe({}, {
        logger.log(TAG, it)
        view.showGenericError()
      })
    )
  }

  private fun showMoreMethods() {
    adyenPaymentInteractor.removePreSelectedPaymentMethod()
    view.showMoreMethods()
  }

  private fun sendPaymentEvent() {
    disposables.add(Single.just(transactionBuilder)
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .subscribe { transactionBuilder ->
        stopTimingForPurchaseEvent(true)
        analytics.sendPaymentEvent(
          transactionBuilder.domain,
          transactionBuilder.skuId,
          transactionBuilder.amount().toString(),
          mapPaymentToAnalytics(paymentType),
          transactionBuilder.type
        )
      })
  }

  private fun sendRevenueEvent() {
    disposables.add(Single.just(transactionBuilder)
      .observeOn(networkScheduler)
      .doOnSuccess { transactionBuilder ->
        analytics.sendRevenueEvent(
          adyenPaymentInteractor.convertToFiat(
            transactionBuilder.amount().toDouble(),
            BillingAnalytics.EVENT_REVENUE_CURRENCY
          )
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .blockingGet()
            .amount
            .setScale(2, BigDecimal.ROUND_UP)
            .toString()
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun sendPaymentSuccessEvent(txId: String) {
    disposables.add(Single.just(transactionBuilder)
      .observeOn(networkScheduler)
      .doOnSuccess { transaction ->
        analytics.sendPaymentSuccessEvent(
          packageName = transactionBuilder.domain,
          skuDetails = transaction.skuId,
          value = transaction.amount().toString(),
          purchaseDetails = mapPaymentToAnalytics(paymentType),
          transactionType = transaction.type,
          txId = txId,
          valueUsd = transaction.amountUsd.toString()
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun sendPaymentErrorEvent(
    refusalCode: Int?,
    refusalReason: String?,
    riskRules: String? = null
  ) {
    disposables.add(Single.just(transactionBuilder)
      .observeOn(networkScheduler)
      .doOnSuccess { transaction ->
        stopTimingForPurchaseEvent(false)
        analytics.sendPaymentErrorWithDetailsAndRiskEvent(
          transactionBuilder.domain,
          transaction.skuId,
          transaction.amount().toString(),
          mapPaymentToAnalytics(paymentType),
          transaction.type,
          refusalCode.toString(),
          refusalReason,
          riskRules
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun mapPaymentToAnalytics(paymentType: String): String =
    if (paymentType == PaymentType.CARD.name) {
      PaymentMethodsAnalytics.PAYMENT_METHOD_CC
    } else if (paymentType == PaymentType.GIROPAY.name) {
      PaymentMethodsAnalytics.PAYMENT_METHOD_GIROPAY
    } else {
      PaymentMethodsAnalytics.PAYMENT_METHOD_PP
    }

  private fun mapPaymentToService(paymentType: String): AdyenPaymentRepository.Methods =
    when (paymentType) {
      PaymentType.CARD.name -> {
        AdyenPaymentRepository.Methods.CREDIT_CARD
      }
      PaymentType.GIROPAY.name -> {
        AdyenPaymentRepository.Methods.GIROPAY
      }
      else -> {
        AdyenPaymentRepository.Methods.PAYPAL
      }
    }

  private fun mapToAdyenBillingAddress(billingAddressModel: BillingAddressModel?): AdyenBillingAddress? =
    billingAddressModel?.let {
      AdyenBillingAddress(it.address, it.city, it.zipcode, it.number, it.state, it.country)
    }

  private fun createBundle(
    hash: String?,
    orderReference: String?,
    purchaseUid: String?
  ): Single<PurchaseBundleModel> =
    adyenPaymentInteractor
      .getCompletePurchaseBundle(
        transactionBuilder.type,
        transactionBuilder.domain,
        transactionBuilder.skuId,
        purchaseUid,
        orderReference,
        hash,
        networkScheduler
      )
      .map { mapPaymentMethodId(it) }

  private fun mapPaymentMethodId(purchaseBundleModel: PurchaseBundleModel): PurchaseBundleModel {
    val bundle = purchaseBundleModel.bundle
    if (paymentType == PaymentType.CARD.name) {
      bundle.putString(
        InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
        PaymentMethodsView.PaymentMethodId.CREDIT_CARD.id
      )
    } else if (paymentType == PaymentType.PAYPAL.name) {
      bundle.putString(
        InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
        PaymentMethodsView.PaymentMethodId.PAYPAL.id
      )
    } else if (paymentType == PaymentType.GIROPAY.name) {
      bundle.putString(
        InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
        PaymentMethodsView.PaymentMethodId.GIROPAY.id
      )
    }
    return PurchaseBundleModel(bundle, purchaseBundleModel.renewal)
  }

  private fun handleBuyAnalytics(transactionBuilder: TransactionBuilder) = if (isPreSelected) {
    analytics.sendPreSelectedPaymentMethodEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      mapPaymentToService(paymentType).transactionType,
      transactionBuilder.type,
      BillingAnalytics.ACTION_BUY
    )
  } else {
    analytics.sendPaymentConfirmationEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      mapPaymentToService(paymentType).transactionType,
      transactionBuilder.type,
      BillingAnalytics.ACTION_BUY
    )
  }

  private fun handleAdyenErrorBack() {
    disposables.add(view.adyenErrorBackClicks()
      .observeOn(viewScheduler)
      .doOnNext {
        adyenPaymentInteractor.removePreSelectedPaymentMethod()
        view.showMoreMethods()
      }
      .subscribe({}, {
        logger.log(TAG, it)
        view.showGenericError()
      })
    )
  }

  private fun handleAdyenErrorBackToCard() {
    disposables.add(view.adyenErrorBackToCardClicks()
      .observeOn(viewScheduler)
      .doOnNext {
        adyenPaymentInteractor.removePreSelectedPaymentMethod()
        if (priceAmount != null && priceCurrency != null) {
          handleBuyClick(priceAmount!!, priceCurrency!!)
        }
        view.showBackToCard()
      }
      .subscribe({}, {
        logger.log(TAG, it)
        view.showGenericError()
      })
    )
  }

  private fun handleAdyenErrorCancel() {
    disposables.add(view.adyenErrorCancelClicks()
      .observeOn(viewScheduler)
      .doOnNext {
        view.close(adyenPaymentInteractor.mapCancellation())
      }
      .subscribe({}, {
        logger.log(TAG, it)
        view.showGenericError()
      })
    )
  }

  private fun handleAdyenAction(paymentModel: PaymentModel) {
    if (paymentModel.action != null) {
      when (val type = paymentModel.action?.type) {
        REDIRECT -> {
          action3ds = type
          paymentAnalytics.send3dsStart(action3ds)
          cachedPaymentData = paymentModel.paymentData
          cachedUid = paymentModel.uid
          navigator.navigateToUriForResult(paymentModel.redirectUrl)
          waitingResult = true
        }
        THREEDS2, THREEDS2FINGERPRINT, THREEDS2CHALLENGE -> {
          action3ds = type
          paymentAnalytics.send3dsStart(action3ds)
          cachedUid = paymentModel.uid
          view.handle3DSAction(paymentModel.action!!)
          waitingResult = true
        }
        else -> {
          logger.log(TAG, "Unknown adyen action: $type")
          view.showGenericError()
        }
      }
    }
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

  private fun handleErrors(error: Error, code: Int? = null) {
    when {
      error.isNetworkError -> view.showNetworkError()
      error.errorInfo?.errorType == ErrorType.INVALID_CARD -> view.showInvalidCardError()
      error.errorInfo?.errorType == ErrorType.CARD_SECURITY_VALIDATION -> view.showSecurityValidationError()
      error.errorInfo?.errorType == ErrorType.OUTDATED_CARD -> view.showOutdatedCardError()
      error.errorInfo?.errorType == ErrorType.ALREADY_PROCESSED -> view.showAlreadyProcessedError()
      error.errorInfo?.errorType == ErrorType.PAYMENT_ERROR -> view.showPaymentError()
      error.errorInfo?.errorType == ErrorType.INVALID_COUNTRY_CODE -> view.showSpecificError(R.string.unknown_error)
      error.errorInfo?.errorType == ErrorType.PAYMENT_NOT_SUPPORTED_ON_COUNTRY -> view.showSpecificError(
        R.string.purchase_error_payment_rejected
      )
      error.errorInfo?.errorType == ErrorType.CURRENCY_NOT_SUPPORTED -> view.showSpecificError(R.string.purchase_card_error_general_1)
      error.errorInfo?.errorType == ErrorType.CVC_LENGTH -> view.showCvvError()
      error.errorInfo?.errorType == ErrorType.TRANSACTION_AMOUNT_EXCEEDED -> view.showSpecificError(
        R.string.purchase_card_error_no_funds
      )
      error.errorInfo?.httpCode != null -> {
        val resId = servicesErrorCodeMapper.mapError(error.errorInfo?.errorType)
        if (error.errorInfo?.httpCode == HTTP_FRAUD_CODE) handleFraudFlow(resId, emptyList())
        else view.showSpecificError(resId)
      }
      else -> {
        val isBackToCardAction = adyenErrorCodeMapper.needsCardRepeat(code ?: 0)
        view.showSpecificError(adyenErrorCodeMapper.map(code ?: 0), isBackToCardAction)
      }
    }
  }

  private fun stopTimingForPurchaseEvent(success: Boolean) {
    val paymentMethod = when (paymentType) {
      PaymentType.PAYPAL.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_PP
      PaymentType.CARD.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_CC
      PaymentType.GIROPAY.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_GIROPAY
      else -> return
    }
    paymentAnalytics.stopTimingForPurchaseEvent(paymentMethod, success, isPreSelected)
  }
}