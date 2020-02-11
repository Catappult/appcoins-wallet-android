package com.asfoundation.wallet.topup.payment

import android.os.Bundle
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.adyen.TransactionResponse.Status
import com.appcoins.wallet.billing.adyen.TransactionResponse.Status.CANCELED
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.topup.CurrencyData
import com.asfoundation.wallet.topup.TopUpData
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.iab.Navigator
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
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
                          private val currencyData: CurrencyData,
                          private val selectedCurrency: String,
                          private val navigator: Navigator,
                          private val billingMessagesMapper: BillingMessagesMapper,
                          private val adyenPaymentInteractor: AdyenPaymentInteractor,
                          private val bonusValue: String,
                          private val adyenErrorCodeMapper: AdyenErrorCodeMapper) {

  private var waitingResult = false

  fun present(savedInstanceState: Bundle?) {
    if (savedInstanceState != null) {
      waitingResult = savedInstanceState.getBoolean(WAITING_RESULT)
    }
    loadPaymentMethodInfo(savedInstanceState)
    handleErrorDismissEvent()
    handleForgetCardClick()

    handleRedirectResponse()
    handleSupportClicks()
    handleTryAgainClicks()
  }

  private fun handleSupportClicks() {
    disposables.add(
        view.getSupportClicks()
            .throttleFirst(50, TimeUnit.MILLISECONDS)
            .flatMapCompletable { adyenPaymentInteractor.showSupport() }
            .subscribeOn(viewScheduler)
            .subscribe()
    )
  }

  private fun handleTryAgainClicks() {
    disposables.add(
        view.getTryAgainClicks()
            .throttleFirst(50, TimeUnit.MILLISECONDS)
            .doOnNext { view.hideSpecificError() }
            .subscribeOn(viewScheduler)
            .subscribe()
    )
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
              handleTopUpClick(it.priceAmount, it.priceCurrency)
            } else if (paymentType == PaymentType.PAYPAL.name) {
              launchPaypal(it.paymentMethodInfo!!, it.priceAmount, it.priceCurrency)
            }
          }
        }
        .subscribe())
  }

  private fun launchPaypal(paymentMethodInfo: PaymentMethod, priceAmount: BigDecimal,
                           priceCurrency: String) {
    disposables.add(
        adyenPaymentInteractor.makeTopUpPayment(paymentMethodInfo, false,
            returnUrl, priceAmount.toString(), priceCurrency,
            mapPaymentToService(paymentType).transactionType,
            transactionType, appPackage)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .filter { !waitingResult }
            .doOnSuccess { handlePaymentModel(it, priceAmount, priceCurrency) }
            .subscribe())
  }

  private fun handleErrorDismissEvent() {
    disposables.add(Observable.merge(view.errorDismisses(), view.errorCancels(),
        view.errorPositiveClicks())
        .doOnNext { navigator.popViewWithError() }
        .doOnError { it.printStackTrace() }
        .subscribe())
  }

  private fun handleTopUpClick(priceAmount: BigDecimal, priceCurrency: String) {
    disposables.add(Observable.combineLatest(view.topUpButtonClicked(), view.retrievePaymentData(),
        BiFunction { _: Any, paymentData: AdyenCardWrapper ->
          paymentData
        })
        .doOnNext {
          view.showLoading()
          view.setFinishingPurchase()
        }
        .observeOn(networkScheduler)
        .flatMapSingle {
          adyenPaymentInteractor.makeTopUpPayment(it.cardPaymentMethod, it.shouldStoreCard,
              returnUrl, priceAmount.toString(), priceCurrency,
              mapPaymentToService(paymentType).transactionType, transactionType, appPackage)
        }
        .observeOn(viewScheduler)
        .flatMapCompletable { handlePaymentResult(it, priceAmount, priceCurrency) }
        .subscribe())
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

  private fun handlePaymentDetails(priceAmount: BigDecimal,
                                   priceCurrency: String) {
    disposables.add(view.getPaymentDetails()
        .observeOn(viewScheduler)
        .doOnNext {
          view.hideKeyboard()
          view.setFinishingPurchase()
        }
        .observeOn(networkScheduler)
        .flatMapSingle { adyenPaymentInteractor.submitRedirect(it.uid, it.details, it.paymentData) }
        .observeOn(viewScheduler)
        .flatMapCompletable { handlePaymentResult(it, priceAmount, priceCurrency) }
        .subscribe())
  }

  private fun handlePaymentResult(paymentModel: PaymentModel, priceAmount: BigDecimal,
                                  priceCurrency: String): Completable {
    return when {
      paymentModel.resultCode.equals("AUTHORISED", ignoreCase = true) -> {
        adyenPaymentInteractor.getTransaction(paymentModel.uid)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .flatMapCompletable {
              if (it.status == Status.COMPLETED) {
                Completable.fromAction {
                  val bundle = createBundle(priceAmount, priceCurrency)
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
        paymentModel.refusalCode?.let { code ->
          if (code == 24) {
            view.showCvvError()
          } else {
            view.showSpecificError(adyenErrorCodeMapper.map(code))
          }
        }
      }
      paymentModel.status == CANCELED -> Completable.fromAction { view.cancelPayment() }
      else -> Completable.fromAction { view.showGenericError() }
    }
  }

  private fun handlePaymentModel(paymentModel: PaymentModel,
                                 priceAmount: BigDecimal, priceCurrency: String) {
    if (paymentModel.error.hasError) {
      if (paymentModel.error.isNetworkError) view.showNetworkError()
      else view.showGenericError()
    } else {
      view.showLoading()
      view.setRedirectComponent(paymentModel.uid, paymentModel.action!!)
      handlePaymentDetails(priceAmount, priceCurrency)
      navigator.navigateToUriForResult(paymentModel.redirectUrl)
      waitingResult = true

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

  private fun createBundle(priceAmount: BigDecimal, priceCurrency: String): Bundle {
    return billingMessagesMapper.topUpBundle(priceAmount.toPlainString(), priceCurrency, bonusValue)
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