package com.asfoundation.wallet.billing.adyen

import android.os.Bundle
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.adyen.TransactionResponse.Status
import com.appcoins.wallet.billing.adyen.TransactionResponse.Status.*
import com.appcoins.wallet.billing.util.Error
import com.asfoundation.wallet.analytics.FacebookEventLogger
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper.Companion.CVC_DECLINED
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper.Companion.FRAUD
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.Navigator
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
                            private val returnUrl: String,
                            private val analytics: BillingAnalytics,
                            private val domain: String,
                            private val origin: String?,
                            private val adyenPaymentInteractor: AdyenPaymentInteractor,
                            private val transactionBuilder: Single<TransactionBuilder>,
                            private val navigator: Navigator,
                            private val paymentType: String,
                            private val transactionType: String,
                            private val amount: BigDecimal,
                            private val currency: String,
                            private val isPreSelected: Boolean,
                            private val adyenErrorCodeMapper: AdyenErrorCodeMapper,
                            private val gamificationLevel: Int,
                            private val formatter: CurrencyFormatUtils) {

  private var waitingResult = false

  fun present(savedInstanceState: Bundle?) {
    savedInstanceState?.let { waitingResult = it.getBoolean(WAITING_RESULT) }
    loadPaymentMethodInfo(savedInstanceState)
    handleBack()
    handleErrorDismissEvent()
    handleForgetCardClick()

    handleRedirectResponse()
    handlePaymentDetails()
    handleAdyenErrorBack()
    handleAdyenErrorCancel()
    handleSupportClicks()
    if (isPreSelected) handleMorePaymentsClick()
  }

  private fun handleSupportClicks() {
    disposables.add(
        Observable.merge(view.getAdyenSupportIconClicks(), view.getAdyenSupportLogoClicks())
            .throttleFirst(50, TimeUnit.MILLISECONDS)
            .flatMapCompletable { adyenPaymentInteractor.showSupport(gamificationLevel) }
            .subscribe()
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
          adyenPaymentInteractor.loadPaymentInfo(mapPaymentToService(paymentType),
              amount.toString(), currency)
              .observeOn(viewScheduler)
              .doOnSuccess {
                view.hideLoadingAndShowView()
                if (it.error.hasError) {
                  if (it.error.isNetworkError) view.showNetworkError()
                  else view.showGenericError()
                } else {
                  view.finishCardConfiguration(it.paymentMethodInfo!!, it.isStored, true, null)
                }
              }
        }
        .subscribe())
  }

  private fun loadPaymentMethodInfo(savedInstanceState: Bundle?) {
    view.showLoading()
    disposables.add(
        adyenPaymentInteractor.loadPaymentInfo(mapPaymentToService(paymentType), amount.toString(),
            currency)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              if (it.error.hasError) {
                view.hideLoadingAndShowView()
                if (it.error.isNetworkError) view.showNetworkError()
                else view.showGenericError()
              } else {
                val amount = formatter.formatCurrency(it.priceAmount, WalletCurrency.FIAT)
                view.showProductPrice(amount, it.priceCurrency)
                if (paymentType == PaymentType.CARD.name) {
                  view.hideLoadingAndShowView()
                  sendPaymentMethodDetailsEvent(BillingAnalytics.PAYMENT_METHOD_CC)
                  view.finishCardConfiguration(it.paymentMethodInfo!!, it.isStored, false,
                      savedInstanceState)
                  handleBuyClick(it.priceAmount, it.priceCurrency)
                } else if (paymentType == PaymentType.PAYPAL.name) {
                  launchPaypal(it.paymentMethodInfo!!, it.priceAmount, it.priceCurrency)
                }
              }
            }
            .subscribe())
  }

  private fun launchPaypal(paymentMethodInfo: PaymentMethod, priceAmount: BigDecimal,
                           priceCurrency: String) {
    disposables.add(transactionBuilder.flatMap {
      adyenPaymentInteractor.makePayment(paymentMethodInfo, false, returnUrl,
          priceAmount.toString(), priceCurrency, it.orderReference,
          mapPaymentToService(paymentType).transactionType, origin, domain, it.payload,
          it.skuId, it.callbackUrl, it.type, it.toAddress())
    }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .filter { !waitingResult }
        .doOnSuccess {
          view.hideLoadingAndShowView()
          handlePaymentModel(it)
        }
        .subscribe())
  }

  private fun handlePaymentModel(paymentModel: PaymentModel) {
    if (paymentModel.error.hasError) {
      if (paymentModel.error.isNetworkError) view.showNetworkError()
      else view.showGenericError()
    } else {
      view.showLoading()
      view.lockRotation()
      view.setRedirectComponent(paymentModel.action!!, paymentModel.uid)
      navigator.navigateToUriForResult(paymentModel.redirectUrl)
      waitingResult = true
      sendPaymentMethodDetailsEvent(mapPaymentToAnalytics(paymentType))
    }
  }

  private fun handleBuyClick(priceAmount: BigDecimal, priceCurrency: String) {
    disposables.add(
        view.buyButtonClicked()
            .flatMapSingle {
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
              transactionBuilder
                  .flatMap {
                    handleBuyAnalytics(it)
                    adyenPaymentInteractor.makePayment(adyenCard.cardPaymentMethod,
                        adyenCard.shouldStoreCard, returnUrl, priceAmount.toString(), priceCurrency,
                        it.orderReference, mapPaymentToService(paymentType).transactionType, origin,
                        domain, it.payload, it.skuId, it.callbackUrl, it.type, it.toAddress())
                  }
            }
            .observeOn(viewScheduler)
            .flatMapCompletable {
              handlePaymentResult(it.uid, it.resultCode, it.refusalCode, it.refusalReason,
                  it.status, it.error)
            }
            .subscribe())
  }

  private fun handlePaymentResult(uid: String, resultCode: String,
                                  refusalCode: Int?, refusalReason: String?,
                                  status: Status,
                                  error: Error): Completable {
    return when {
      resultCode.equals("AUTHORISED", true) -> {
        adyenPaymentInteractor.getTransaction(uid)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .flatMapCompletable {
              when {
                it.status == COMPLETED -> {
                  sendPaymentSuccessEvent()
                  createBundle(it.hash, it.orderReference)
                      .doOnSuccess {
                        sendPaymentEvent()
                        sendRevenueEvent()
                      }
                      .subscribeOn(networkScheduler)
                      .observeOn(viewScheduler)
                      .flatMapCompletable {
                        Completable.fromAction { view.showSuccess() }
                            .andThen(
                                Completable.timer(view.getAnimationDuration(),
                                    TimeUnit.MILLISECONDS))
                            .andThen(Completable.fromAction { navigator.popView(it) })
                      }
                }
                isPaymentFailed(it.status) -> {
                  Completable.fromAction {
                    sendPaymentErrorEvent(it.error.code,
                        buildRefusalReason(it.status, it.error.message))
                    view.showGenericError()
                  }
                      .subscribeOn(viewScheduler)
                }
                else -> {
                  sendPaymentErrorEvent(it.error.code, it.status.toString())
                  Completable.fromAction { view.showGenericError() }
                }
              }
            }
      }
      refusalReason != null -> Completable.fromAction {
        sendPaymentErrorEvent(refusalCode, refusalReason)
        refusalCode?.let { code ->
          when (code) {
            CVC_DECLINED -> view.showCvvError()
            FRAUD -> handleFraudFlow()
            else -> view.showSpecificError(adyenErrorCodeMapper.map(code))
          }
        }
      }
      error.hasError -> Completable.fromAction {
        sendPaymentErrorEvent(error.code, error.message)
        if (error.isNetworkError) view.showNetworkError()
        else view.showGenericError()
      }
      status == CANCELED -> Completable.fromAction { view.showMoreMethods() }
      else -> Completable.fromAction {
        sendPaymentErrorEvent(error.code, "Generic Error")
        view.showGenericError()
      }
    }
  }

  private fun handleFraudFlow() {
    disposables.add(
        adyenPaymentInteractor.isWalletBlocked()
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              if (it.not()) view.showGenericError()
            }
            .observeOn(networkScheduler)
            .flatMap {
              adyenPaymentInteractor.isWalletVerified()
                  .observeOn(viewScheduler)
                  .doOnSuccess {
                    if (it) view.showGenericError()
                    else view.showWalletValidation()
                  }
            }
            .observeOn(viewScheduler)
            .subscribe({}, {
              it.printStackTrace()
              view.showGenericError()
            })
    )
  }

  private fun buildRefusalReason(status: Status, message: String?): String {
    return message?.let { "$status : $it" } ?: status.toString()
  }

  private fun isPaymentFailed(status: Status): Boolean {
    return status == FAILED || status == CANCELED || status == INVALID_TRANSACTION
  }

  private fun handlePaymentDetails() {
    disposables.add(view.getPaymentDetails()
        .observeOn(networkScheduler)
        .flatMapSingle { adyenPaymentInteractor.submitRedirect(it.uid, it.details, it.paymentData) }
        .observeOn(viewScheduler)
        .flatMapCompletable {
          handlePaymentResult(it.uid, it.resultCode, it.refusalCode, it.refusalReason, it.status,
              it.error)
        }
        .subscribe())
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(WAITING_RESULT, waitingResult)
  }

  private fun sendPaymentMethodDetailsEvent(paymentMethod: String) {
    disposables.add(transactionBuilder.subscribe { transactionBuilder: TransactionBuilder ->
      analytics.sendPaymentMethodDetailsEvent(domain, transactionBuilder.skuId,
          transactionBuilder.amount()
              .toString(), paymentMethod, transactionBuilder.type)
    })
  }

  private fun handleErrorDismissEvent() {
    disposables.add(view.errorDismisses()
        .observeOn(viewScheduler)
        .doOnNext { navigator.popViewWithError() }
        .subscribe())
  }

  private fun handleBack() {
    disposables.add(view.backEvent()
        .observeOn(networkScheduler)
        .flatMapSingle { transactionBuilder }
        .doOnNext { transaction ->
          handlePaymentMethodAnalytics(transaction)
        }
        .observeOn(viewScheduler)
        .subscribe({}, { view.showGenericError() }))
  }

  private fun handlePaymentMethodAnalytics(transaction: TransactionBuilder) {
    if (isPreSelected) {
      analytics.sendPreSelectedPaymentMethodEvent(domain, transaction.skuId,
          transaction.amount()
              .toString(), mapPaymentToService(paymentType).transactionType,
          transaction.type, "cancel")
      view.close(adyenPaymentInteractor.mapCancellation())
    } else {
      analytics.sendPaymentConfirmationEvent(domain, transaction.skuId,
          transaction.amount()
              .toString(), mapPaymentToService(paymentType).transactionType,
          transaction.type, "back")
      view.showMoreMethods()
    }
  }

  private fun handleMorePaymentsClick() {
    disposables.add(
        view.getMorePaymentMethodsClicks()
            .observeOn(networkScheduler)
            .flatMapSingle { transactionBuilder }
            .doOnNext { transaction ->
              handleMorePaymentsAnalytics(transaction)
            }
            .observeOn(viewScheduler)
            .doOnNext { showMoreMethods() }
            .subscribe())
  }

  private fun handleMorePaymentsAnalytics(transaction: TransactionBuilder) {
    analytics.sendPreSelectedPaymentMethodEvent(domain, transaction.skuId,
        transaction.amount()
            .toString(), mapPaymentToService(paymentType).transactionType,
        transaction.type, "other_payments")
  }

  private fun handleRedirectResponse() {
    disposables.add(navigator.uriResults()
        .observeOn(viewScheduler)
        .doOnNext { view.submitUriResult(it) }
        .subscribe())
  }

  private fun showMoreMethods() {
    adyenPaymentInteractor.removePreSelectedPaymentMethod()
    view.showMoreMethods()
  }

  private fun sendPaymentEvent() {
    disposables.add(transactionBuilder.subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .subscribe { transactionBuilder: TransactionBuilder ->
          analytics.sendPaymentEvent(domain, transactionBuilder.skuId,
              transactionBuilder.amount()
                  .toString(), mapPaymentToAnalytics(paymentType),
              transactionBuilder.type)
        })
  }

  private fun sendRevenueEvent() {
    disposables.add(transactionBuilder.subscribe { transactionBuilder: TransactionBuilder ->
      analytics.sendRevenueEvent(adyenPaymentInteractor.convertToFiat(transactionBuilder.amount()
          .toDouble(), FacebookEventLogger.EVENT_REVENUE_CURRENCY)
          .subscribeOn(networkScheduler)
          .observeOn(viewScheduler)
          .blockingGet()
          .amount
          .setScale(2, BigDecimal.ROUND_UP)
          .toString())
    })
  }

  private fun sendPaymentSuccessEvent() {
    disposables.add(transactionBuilder
        .observeOn(networkScheduler)
        .doOnSuccess { transaction ->
          analytics.sendPaymentSuccessEvent(domain, transaction.skuId,
              transaction.amount()
                  .toString(),
              mapPaymentToAnalytics(paymentType), transaction.type)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun sendPaymentErrorEvent(refusalCode: Int?, refusalReason: String?) {
    disposables.add(transactionBuilder
        .observeOn(networkScheduler)
        .doOnSuccess { transaction ->
          analytics.sendPaymentErrorWithDetailsEvent(domain, transaction.skuId,
              transaction.amount()
                  .toString(), mapPaymentToAnalytics(paymentType), transaction.type,
              refusalCode.toString(), refusalReason)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun mapPaymentToAnalytics(paymentType: String): String {
    return if (paymentType == PaymentType.CARD.name) {
      BillingAnalytics.PAYMENT_METHOD_CC
    } else {
      BillingAnalytics.PAYMENT_METHOD_PAYPAL
    }
  }

  private fun mapPaymentToService(paymentType: String): AdyenPaymentRepository.Methods {
    return if (paymentType == PaymentType.CARD.name) {
      AdyenPaymentRepository.Methods.CREDIT_CARD
    } else {
      AdyenPaymentRepository.Methods.PAYPAL
    }
  }

  private fun createBundle(hash: String?, orderReference: String?): Single<Bundle> {
    return transactionBuilder.flatMap {
      adyenPaymentInteractor.getCompletePurchaseBundle(transactionType, domain, it.skuId,
          orderReference, hash, networkScheduler)
    }
        .map { mapPaymentMethodId(it) }
  }

  private fun mapPaymentMethodId(bundle: Bundle): Bundle {
    if (paymentType == PaymentType.CARD.name) {
      bundle.putString(InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
          PaymentMethodsView.PaymentMethodId.CREDIT_CARD.id)
    } else if (paymentType == PaymentType.PAYPAL.name) {
      bundle.putString(InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
          PaymentMethodsView.PaymentMethodId.PAYPAL.id)
    }
    return bundle
  }

  private fun handleBuyAnalytics(transactionBuilder: TransactionBuilder) {
    if (isPreSelected) {
      analytics.sendPreSelectedPaymentMethodEvent(domain, transactionBuilder.skuId,
          transactionBuilder.amount()
              .toString(), mapPaymentToService(paymentType).transactionType,
          transactionBuilder.type, "buy")
    } else {
      analytics.sendPaymentConfirmationEvent(domain, transactionBuilder.skuId,
          transactionBuilder.amount()
              .toString(), mapPaymentToService(paymentType).transactionType,
          transactionBuilder.type, "buy")
    }
  }

  private fun handleAdyenErrorBack() {
    disposables.add(view.adyenErrorBackClicks()
        .observeOn(viewScheduler)
        .doOnNext {
          if (isPreSelected) {
            view.close(adyenPaymentInteractor.mapCancellation())
          } else {
            view.showMoreMethods()
          }
        }
        .subscribe({}, { view.showGenericError() }))
  }

  private fun handleAdyenErrorCancel() {
    disposables.add(view.adyenErrorCancelClicks()
        .observeOn(viewScheduler)
        .doOnNext {
          view.close(adyenPaymentInteractor.mapCancellation())
        }
        .subscribe({}, { view.showGenericError() }))
  }

  fun stop() {
    disposables.clear()
  }

  companion object {

    private const val WAITING_RESULT = "WAITING_RESULT"
  }
}
