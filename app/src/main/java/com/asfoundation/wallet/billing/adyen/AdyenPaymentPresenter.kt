package com.asfoundation.wallet.billing.adyen

import android.os.Bundle
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.adyen.TransactionResponse
import com.appcoins.wallet.billing.util.Error
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
                            private val isPreSelected: Boolean) {

  private var waitingResult = false

  fun present(savedInstanceState: Bundle?) {
    savedInstanceState?.let { waitingResult = it.getBoolean(WAITING_RESULT) }
    loadPaymentMethodInfo(savedInstanceState)
    handleBack()
    handleErrorDismissEvent()
    handleForgetCardClick()

    handleRedirectResponse()
    handlePaymentDetails()
    if (isPreSelected) handleMorePaymentsClick()
  }

  private fun handleForgetCardClick() {
    disposables.add(view.forgetCardClick()
        .observeOn(networkScheduler)
        .flatMapSingle { adyenPaymentInteractor.disablePayments() }
        .observeOn(viewScheduler)
        .doOnNext { success -> if (!success) view.showGenericError() }
        .filter { it }
        .doOnNext { view.showLoading() }
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
                view.showProductPrice(it.priceAmount, it.priceCurrency)
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
    disposables.add(Observable.combineLatest(view.buyButtonClicked(), view.retrievePaymentData(),
        BiFunction { _: Any, adyenCard: AdyenCardWrapper -> adyenCard })
        .observeOn(viewScheduler)
        .doOnNext {
          view.showLoading()
          view.hideKeyboard()
        }
        .observeOn(networkScheduler)
        .flatMapSingle { adyenCard ->
          transactionBuilder
              .flatMap {
                adyenPaymentInteractor.makePayment(adyenCard.cardPaymentMethod,
                    adyenCard.shouldStoreCard, returnUrl,
                    priceAmount.toString(), priceCurrency, it.orderReference,
                    mapPaymentToService(paymentType).transactionType, origin, domain, it.payload,
                    it.skuId, it.callbackUrl, it.type, it.toAddress())
              }
        }
        .observeOn(viewScheduler)
        .flatMapCompletable {
          handlePaymentResult(it.uid, it.resultCode, it.refusalCode, it.refusalReason, it.error)
        }
        .subscribe())
  }

  private fun handlePaymentResult(uid: String, resultCode: String,
                                  refusalCode: Int?, refusalReason: String?,
                                  error: Error): Completable {
    return when {
      resultCode.equals("AUTHORISED", true) -> {
        adyenPaymentInteractor.getTransaction(uid)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .flatMapCompletable {
              when {
                it.status == TransactionResponse.Status.COMPLETED -> {
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
                paymentFailed(it.status) -> {
                  Completable.fromAction { view.showGenericError() }
                      .subscribeOn(viewScheduler)
                }
                else -> {
                  //Non-final state. Chain should restart since getTransaction will emit again
                  Completable.complete()
                }
              }
            }
      }
      error.hasError -> Completable.fromAction {
        if (error.isNetworkError) view.showNetworkError()
        else view.showGenericError()
      }
      refusalReason != null -> Completable.fromAction {
        refusalCode?.let { code -> view.showSpecificError(code) }
      }
      else -> Completable.fromAction {
        view.showGenericError()
      }
    }
  }

  private fun paymentFailed(status: TransactionResponse.Status): Boolean {
    return status == TransactionResponse.Status.FAILED || status == TransactionResponse.Status.CANCELED || status == TransactionResponse.Status.INVALID_TRANSACTION
  }

  private fun handlePaymentDetails() {
    disposables.add(view.getPaymentDetails()
        .observeOn(networkScheduler)
        .flatMapSingle { adyenPaymentInteractor.submitRedirect(it.uid, it.details, it.paymentData) }
        .observeOn(viewScheduler)
        .flatMapCompletable {
          handlePaymentResult(it.uid, it.resultCode, it.refusalCode, it.refusalReason, it.error)
        }
        .subscribe())
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(WAITING_RESULT, waitingResult)
  }

  private fun sendPaymentMethodDetailsEvent(paymentMethod: String) {
    disposables.add(transactionBuilder.subscribe { transactionBuilder: TransactionBuilder ->
      analytics.sendPaymentEvent(domain, transactionBuilder.skuId,
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
        .observeOn(viewScheduler)
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
          transactionBuilder.amount().toString(), mapPaymentToAnalytics(paymentType),
          transactionBuilder.type)
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

  fun stop() {
    disposables.clear()
  }

  companion object {

    private const val WAITING_RESULT = "WAITING_RESULT"
  }
}
