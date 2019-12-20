package com.asfoundation.wallet.topup.payment

import android.content.Context
import android.os.Bundle
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.payments.request.CardPaymentMethod
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.adyen.TransactionResponse
import com.appcoins.wallet.billing.util.Error
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.topup.CurrencyData
import com.asfoundation.wallet.topup.TopUpData
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.iab.Navigator
import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class AdyenTopUpPresenter(private val view: AdyenTopUpView,
                          private val context: Context?,
                          private val appPackage: String,
                          private val viewScheduler: Scheduler,
                          private val networkScheduler: Scheduler,
                          private val disposables: CompositeDisposable,
                          private val paymentType: String,
                          private val transactionType: String,
                          private val amount: String,
                          private val currency: String,
                          private val currencyData: CurrencyData,
                          private val selectedCurrency: String,
                          private val navigator: Navigator,
                          private val billingMessagesMapper: BillingMessagesMapper,
                          private val adyenPaymentInteractor: AdyenPaymentInteractor,
                          private val bonusValue: String) {

  private var waitingResult = false

  fun present(savedInstanceState: Bundle?) {
    if (savedInstanceState != null) {
      waitingResult = savedInstanceState.getBoolean(WAITING_RESULT)
    }
    loadPaymentMethodInfo(savedInstanceState)
    handleErrorDismissEvent()
    handleTopUpClick()
    handleForgetCardClick()

    handleRedirectResponse()
    handlePaymentDetails()
  }

  private fun loadPaymentMethodInfo(savedInstanceState: Bundle?) {
    disposables.add(convertAmount()
        .flatMap {
          adyenPaymentInteractor.loadPaymentInfo(mapPaymentToService(paymentType), it.toString(),
              currency)
        }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          view.hideLoading()
          if (it.error.hasError) {
            if (it.error.isNetworkError) view.showNetworkError()
            else view.showGenericError()
          } else {
            view.showValues(it.priceAmount, it.priceCurrency)
            if (paymentType == PaymentType.CARD.name) {
              view.finishCardConfiguration(it.paymentMethodInfo!!, it.isStored, false,
                  savedInstanceState)
            } else if (paymentType == PaymentType.PAYPAL.name) {
              launchPaypal(it.paymentMethodInfo!!)
            }
          }
        }
        .subscribe())
  }

  private fun launchPaypal(paymentMethodInfo: PaymentMethod) {
    disposables.add(
        if (context != null) {
          adyenPaymentInteractor.makeTopUpPayment(paymentMethodInfo, view.provideReturnUrl(),
              amount, currency, mapPaymentToService(paymentType).transactionType, transactionType,
              appPackage)
        } else {
          Single.just(PaymentModel(Error()))
        }
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .filter { !waitingResult }
            .doOnSuccess { handlePaymentModel(it) }
            .subscribe())
  }

  private fun handleErrorDismissEvent() {
    disposables.add(Observable.merge(view.errorDismisses(), view.errorCancels(),
        view.errorPositiveClicks())
        .doOnNext { navigator.popViewWithError() }
        .doOnError { it.printStackTrace() }
        .subscribe())
  }

  private fun handleTopUpClick() {
    disposables.add(Observable.combineLatest(view.topUpButtonClicked(), view.retrievePaymentData(),
        BiFunction { _: Any, paymentData: CardPaymentMethod ->
          paymentData
        })
        .observeOn(networkScheduler)
        .flatMapSingle {
          adyenPaymentInteractor.makeTopUpPayment(it, view.provideReturnUrl(),
              amount, currency, mapPaymentToService(paymentType).transactionType, transactionType,
              appPackage)
        }
        .observeOn(viewScheduler)
        .flatMapCompletable { handlePaymentResult(it) }
        .subscribe())
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
              amount, currency)
              .observeOn(viewScheduler)
              .doOnSuccess {
                view.hideLoading()
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

  private fun handleRedirectResponse() {
    disposables.add(navigator.uriResults()
        .observeOn(viewScheduler)
        .doOnNext { view.submitUriResult(it) }
        .subscribe())
  }

  private fun handlePaymentDetails() {
    disposables.add(view.getPaymentDetails()
        .observeOn(networkScheduler)
        .flatMapSingle { adyenPaymentInteractor.submitRedirect(it.uid, it.details, it.paymentData) }
        .observeOn(viewScheduler)
        .flatMapCompletable { handlePaymentResult(PaymentModel(Error())) }
        .subscribe())
  }

  private fun handlePaymentResult(paymentModel: PaymentModel): Completable {
    return when {
      paymentModel.resultCode == "AUTHORISED" -> {
        adyenPaymentInteractor.getTransaction(paymentModel.uid)
            .flatMapCompletable {
              if (it.status == TransactionResponse.Status.COMPLETED) {
                Completable.fromAction {
                  val bundle = createBundle(paymentModel.uid).blockingGet()
                  waitingResult = false
                  navigator.popView(bundle)
                }
              } else {
                Completable.fromAction { view.showGenericError() }
              }
            }
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

  private fun handlePaymentModel(paymentModel: PaymentModel) {
    if (!paymentModel.error.hasError) {
      view.showLoading()
      view.setRedirectComponent(paymentModel.uid, paymentModel.action!!)
      waitingResult = true
    } else {
      if (paymentModel.error.isNetworkError) view.showNetworkError()
      else view.showGenericError()
    }
  }

  private fun convertAmount()
      : Single<BigDecimal> {
    return if (selectedCurrency == TopUpData.FIAT_CURRENCY) {
      Single.just(
          BigDecimal(currencyData.fiatValue))
    } else adyenPaymentInteractor.convertToLocalFiat(
        BigDecimal(currencyData.appcValue).toDouble())
        .map(FiatValue::amount)
  }

  private fun createBundle(uid: String)
      : Single<Bundle> {
    return adyenPaymentInteractor.getTransactionAmount(
        uid) //TODO change to use the values sent in the first response
        .retryWhen { errors ->
          val counter = AtomicInteger()
          errors.takeWhile { counter.getAndIncrement() != 3 }
              .flatMap { Flowable.timer(counter.get().toLong(), TimeUnit.SECONDS) }
        }
        .map { price ->
          billingMessagesMapper.topUpBundle(price.value, price.currency, bonusValue)
        }
  }

  private fun mapPaymentToService(paymentType: String)
      : AdyenPaymentRepository.Methods {
    return if (paymentType == PaymentType.CARD.name) {
      AdyenPaymentRepository.Methods.CREDIT_CARD
    } else {
      AdyenPaymentRepository.Methods.PAYPAL
    }
  }

  fun stop() {
    disposables.clear()
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(WAITING_RESULT, waitingResult)
  }

  companion object {
    private const val WAITING_RESULT = "WAITING_RESULT"
  }

}