package com.asfoundation.wallet.billing.adyen

import android.os.Bundle
import com.adyen.checkout.redirect.RedirectUtil
import com.appcoins.wallet.billing.adyen.AdyenPaymentService
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.asfoundation.wallet.analytics.FacebookEventLogger
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.Navigator
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

class AdyenPaymentPresenter(private val view: AdyenPaymentView,
                            private val disposables: CompositeDisposable,
                            private val viewScheduler: Scheduler,
                            private val networkScheduler: Scheduler,
                            private val analytics: BillingAnalytics,
                            private val domain: String,
                            private val adyenPaymentInteractor: AdyenPaymentInteractor,
                            private val transactionBuilder: Single<TransactionBuilder>,
                            private val navigator: Navigator,
                            private val paymentType: String,
                            private val amount: BigDecimal,
                            private val currency: String,
                            private val isPreSelected: Boolean) {

  private var waitingResult = false

  fun present(savedInstanceState: Bundle?) {
    savedInstanceState?.let { waitingResult = it.getBoolean(WAITING_RESULT) }
    loadPaymentMethodInfo()
    handleBack()
    handleErrorDismissEvent()
    handleBuyClick()

    handleRedirectResponse()
    handlePaymentDetails()
    convertAmount()
    if (isPreSelected) handleMorePaymentsClick()
  }

  private fun convertAmount() {
    disposables.add(
        adyenPaymentInteractor.convertToFiat(amount.toDouble(), currency)
            .doOnSuccess {
              view.showProductPrice(it.amount.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                  it.currency)
            }.subscribe())
  }

  private fun loadPaymentMethodInfo() {
    disposables.add(
        adyenPaymentInteractor.loadPaymentInfo(mapPaymentToService(paymentType), amount.toString(),
            currency)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              view.hideLoading()
              if (it.error.hasError) {
                if (it.error.isNetworkError) view.showNetworkError()
                else view.showGenericError()
              } else {
                if (paymentType == PaymentType.CARD.name) view.finishCardConfiguration(
                    it.paymentMethodInfo!!)
                else {
                  launchPaypal()
                }
              }
            }
            .doOnError { view.showGenericError() }
            .subscribe())
  }

  private fun launchPaypal() {
    disposables.add(transactionBuilder.flatMap {
      adyenPaymentInteractor.makePayment(amount.toString(), currency, it.orderReference, null, null,
          null, null, mapPaymentToService(paymentType).name, RedirectUtil.REDIRECT_RESULT_SCHEME)
    }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .filter { !waitingResult }
        .doOnSuccess {
          if (!it.error.hasError) {
            view.showLoading()
            view.lockRotation()
            view.setRedirectComponent(it.action!!, it.paymentData)
            navigator.navigateToUriForResult(it.redirectUrl)
            waitingResult = true
            sendPaymentMethodDetailsEvent(mapPaymentToAnalytics(paymentType))
          } else {
            if (it.error.isNetworkError) view.showNetworkError()
            else view.showGenericError()
          }
        }
        .subscribe())
  }

  private fun handleBuyClick() {
    disposables.add(Observable.zip(view.buyButtonClicked(), view.retrievePaymentData(),
        BiFunction { _: Any, paymentData: PaymentData ->
          paymentData
        })
        .observeOn(networkScheduler)
        .flatMapSingle { paymentData ->
          transactionBuilder
              .flatMap {
                adyenPaymentInteractor.makePayment(amount.toString(), currency,
                    it.orderReference,
                    paymentData.encryptedCardNumber, paymentData.encryptedExpiryMonth,
                    paymentData.encryptedExpiryYear, paymentData.encryptedSecurityCode,
                    mapPaymentToService(paymentType).name, it.callbackUrl)
              }
        }
        .observeOn(viewScheduler)
        .flatMapCompletable { handlePaymentResult(it) }
        .subscribe())
  }

  private fun handlePaymentResult(paymentModel: PaymentModel): Completable {
    return when {
      paymentModel.resultCode == "AUTHORISED" -> {
        Completable.fromAction {
          sendPaymentEvent()
          sendRevenueEvent()
          view.showSuccess()
        }
            .andThen(Completable.timer(view.getAnimationDuration(), TimeUnit.MILLISECONDS))
            .andThen(Completable.fromAction { navigator.popView(createBundle()) })
      }
      paymentModel.error.hasError -> Completable.fromAction {
        if (paymentModel.error.isNetworkError) view.showNetworkError()
        else view.showGenericError()
      }
      paymentModel.refusalReason != null -> Completable.fromAction {
        paymentModel.refusalCode?.let { code -> view.showSpecificError(code) }
      }
      else -> Completable.fromAction { view.showGenericError() }
    }
  }

  private fun handlePaymentDetails() {
    disposables.add(Observable.zip(view.getPaymentDetails(), view.getPaymentDetailsData(),
        BiFunction { details: JSONObject, paymentData: String? ->
          Pair(extractPayload(details), paymentData)
        })
        .observeOn(networkScheduler)
        .flatMapSingle {
          adyenPaymentInteractor.submitRedirect(it.first, it.second)
        }
        .observeOn(viewScheduler)
        .flatMapCompletable { handlePaymentResult(it) }
        .subscribe())
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(WAITING_RESULT, waitingResult)
  }

  fun sendPaymentMethodDetailsEvent(paymentMethod: String) {
    disposables.add(transactionBuilder.subscribe { transactionBuilder: TransactionBuilder ->
      analytics.sendPaymentEvent(domain, transactionBuilder.skuId,
          transactionBuilder.amount()
              .toString(), paymentMethod, transactionBuilder.type)
    })
  }

  private fun handleErrorDismissEvent() {
    disposables.add(view.errorDismisses()
        .doOnNext { navigator.popViewWithError() }
        .subscribe())
  }

  private fun handleBack() {
    disposables.add(view.backEvent()
        .observeOn(viewScheduler)
        .doOnNext {
          if (isPreSelected) {
            view.close(adyenPaymentInteractor.mapCancellation())
          } else {
            view.showMoreMethods()
          }
        }.subscribe({}, { view.showGenericError() }))
  }

  private fun handleMorePaymentsClick() {
    disposables.add(
        view.getMorePaymentMethodsClicks()
            .observeOn(viewScheduler)
            .doOnNext { showMoreMethods() }
            .subscribe())
  }

  private fun handleRedirectResponse() {
    disposables.add(navigator.uriResults()
        .doOnNext { view.submitUriResult(it) }
        .subscribe())
  }


  private fun showMoreMethods() {
    adyenPaymentInteractor.removePreSelectedPaymentMethod()
    view.showMoreMethods()
  }

  private fun sendPaymentEvent() {
    disposables.add(transactionBuilder.subscribeOn(networkScheduler).observeOn(
        viewScheduler).subscribe { transactionBuilder: TransactionBuilder ->
      analytics.sendPaymentEvent(domain, transactionBuilder.skuId,
          transactionBuilder.amount()
              .toString(), mapPaymentToAnalytics(paymentType), transactionBuilder.type)
    })
  }

  private fun sendRevenueEvent() {
    disposables.add(transactionBuilder.subscribe { transactionBuilder: TransactionBuilder ->
      analytics.sendRevenueEvent(
          adyenPaymentInteractor.convertToFiat(transactionBuilder.amount()
              .toDouble(), FacebookEventLogger.EVENT_REVENUE_CURRENCY)
              .subscribeOn(networkScheduler)
              .observeOn(viewScheduler)
              .blockingGet()
              .amount
              .setScale(2, BigDecimal.ROUND_UP)
              .toString())
    })
  }

  private fun mapPaymentToAnalytics(paymentType: String): String {
    return if (paymentType == PaymentType.CARD.name) {
      BillingAnalytics.PAYMENT_METHOD_CC
    } else {
      BillingAnalytics.PAYMENT_METHOD_PAYPAL
    }
  }

  private fun mapPaymentToService(paymentType: String): AdyenPaymentService.Methods {
    return if (paymentType == PaymentType.CARD.name) {
      AdyenPaymentService.Methods.CREDIT_CARD
    } else {
      AdyenPaymentService.Methods.PAYPAL
    }
  }

  private fun createBundle(): Bundle {
    return transactionBuilder.flatMap {
      adyenPaymentInteractor.getCompletePurchaseBundle(paymentType, domain, it.skuId,
          it.orderReference, "hash", networkScheduler)
    }
        .map { mapPaymentMethodId(it) }
        .blockingGet()
  }

  private fun extractPayload(payload: JSONObject?): String? {
    return payload?.getString("payload")
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

  fun stop() {
    disposables.clear()
  }

  companion object {

    private const val WAITING_RESULT = "WAITING_RESULT"
  }
}
