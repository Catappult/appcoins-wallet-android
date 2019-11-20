package com.asfoundation.wallet.billing.adyen

import android.os.Bundle
import com.appcoins.wallet.billing.adyen.AdyenPaymentInteractor
import com.appcoins.wallet.billing.adyen.AdyenPaymentService
import com.asfoundation.wallet.analytics.FacebookEventLogger
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.Navigator
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal

class AdyenPaymentPresenter(private val view: AdyenPaymentView,
                            private val disposables: CompositeDisposable,
                            private val viewScheduler: Scheduler,
                            private val networkScheduler: Scheduler,
                            private val analytics: BillingAnalytics,
                            private val domain: String,
                            private val adyenPaymentInteractor: AdyenPaymentInteractor,
                            private val inAppPurchaseInteractor: InAppPurchaseInteractor,
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
                view.finishCardConfiguration(it.paymentMethodInfo!!)
              }
            }
            .subscribe())
  }

  private fun handleBuyClick() {
    disposables.add(view.buyButtonClicked()
        .observeOn(networkScheduler)
        .flatMapSingle { paymentMethod ->
          transactionBuilder
              .flatMap {
                adyenPaymentInteractor.makePayment(amount.toString(), it.orderReference,
                    paymentMethod, it.callbackUrl)
              }
        }.doOnNext { view.handleFinalResponse() } // to improve
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
            view.close(inAppPurchaseInteractor.billingMessagesMapper.mapCancellation())
          } else {
            view.showMoreMethods()
          }
        }.subscribe({}, { view.showGenericError() }))
  }

  private fun sendPaymentEvent(paymentType: PaymentType) {
    disposables.add(transactionBuilder.subscribe { transactionBuilder: TransactionBuilder ->
      analytics.sendPaymentEvent(domain, transactionBuilder.skuId,
          transactionBuilder.amount()
              .toString(), mapPaymentToAnalytics(paymentType), transactionBuilder.type)
    })
  }

  private fun sendRevenueEvent() {
    disposables.add(transactionBuilder.subscribe { transactionBuilder: TransactionBuilder ->
      analytics.sendRevenueEvent(
          inAppPurchaseInteractor.convertToFiat(transactionBuilder.amount()
              .toDouble(), FacebookEventLogger.EVENT_REVENUE_CURRENCY)
              .blockingGet()
              .amount
              .setScale(2, BigDecimal.ROUND_UP)
              .toString())
    })
  }

  private fun mapPaymentToAnalytics(paymentType: PaymentType): String? {
    return if (paymentType == PaymentType.CARD) {
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

  fun stop() {
    disposables.clear()
  }

  companion object {

    private const val WAITING_RESULT = "WAITING_RESULT"
  }
}
