package com.asfoundation.wallet.billing.googlepay

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.CANCELED
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.COMPLETED
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.FAILED
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.FRAUD
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.INVALID_TRANSACTION
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.network.microservices.model.GooglePayWebTransaction
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.android_common.toSingleEvent
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.googlepay.models.GooglePayConst
import com.asfoundation.wallet.billing.googlepay.models.GooglePayResult
import com.asfoundation.wallet.billing.googlepay.usecases.BuildGooglePayUrlUseCase
import com.asfoundation.wallet.billing.googlepay.usecases.CreateGooglePayWebTransactionUseCase
import com.asfoundation.wallet.billing.googlepay.usecases.GetGooglePayResultUseCase
import com.asfoundation.wallet.billing.googlepay.usecases.GetGooglePayUrlUseCase
import com.asfoundation.wallet.billing.googlepay.usecases.WaitForSuccessUseCase
import com.asfoundation.wallet.billing.paypal.usecases.CreateSuccessBundleUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import com.wallet.appcoins.feature.support.data.SupportInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class GooglePayWebViewModel
@Inject
constructor(
    private val getGooglePayUrlUseCase: GetGooglePayUrlUseCase,
    private val createGooglePayWebTransactionUseCase: CreateGooglePayWebTransactionUseCase,
    private val buildGooglePayUrlUseCase: BuildGooglePayUrlUseCase,
    private val waitForSuccessGooglePayWebUseCase: WaitForSuccessUseCase,
    private val createSuccessBundleUseCase: CreateSuccessBundleUseCase,
    private val getGooglePayResultUseCase: GetGooglePayResultUseCase,
    private val adyenPaymentInteractor: AdyenPaymentInteractor,
    private val supportInteractor: SupportInteractor,
    private val inAppPurchaseInteractor: InAppPurchaseInteractor,
    private val rxSchedulers: RxSchedulers,
    private val analytics: BillingAnalytics,
    private val paymentAnalytics: PaymentMethodsAnalytics
) : ViewModel() {

  sealed class State {
    object Start : State()

    data class Error(val stringRes: Int) : State()

    data class WebAuthentication(val url: String) : State()

    data class SuccessPurchase(val bundle: Bundle) : State()

    object GooglePayBack : State()
  }

  private val _state = MutableLiveData<State>(State.Start)
  val state = _state.toSingleEvent()

  private var compositeDisposable: CompositeDisposable = CompositeDisposable()

  val networkScheduler = rxSchedulers.io
  val viewScheduler = rxSchedulers.main

  var uid: String? = null
  var shouldStartPayment = true
  var runningCustomTab = false
  var isFirstRun: Boolean = true

  fun startPayment(
      amount: BigDecimal,
      currency: String,
      transactionBuilder: TransactionBuilder,
      origin: String?
  ) {
    if (shouldStartPayment) {
      shouldStartPayment = false
      sendPaymentConfirmationEvent(transactionBuilder)
      compositeDisposable.add(
          getGooglePayUrlUseCase()
              .subscribeOn(networkScheduler)
              .observeOn(viewScheduler)
              .flatMap { urls ->
                createTransaction(
                        amount = amount,
                        currency = currency,
                        transactionBuilder = transactionBuilder,
                        origin = origin,
                        returnUrl = urls.returnUrl,
                    )
                    .map { transaction ->
                      uid = transaction.uid
                      val googlePayUrl =
                          buildGooglePayUrlUseCase(
                              url = urls.url,
                              sessionId = transaction.sessionId ?: "",
                              sessionData = transaction.sessionData ?: "",
                              price = amount.toString(),
                              currency = currency,
                          )
                      Log.d("url", googlePayUrl)
                      _state.postValue(State.WebAuthentication(googlePayUrl))
                    }
              }
              .subscribe(
                  {},
                  {
                    Log.d(TAG, it.toString())
                    sendPaymentErrorEvent(
                        errorMessage = "GooglePayWeb transaction error.",
                        transactionBuilder = transactionBuilder)
                    _state.postValue(State.Error(R.string.purchase_error_google_pay))
                  }))
    }
  }

  fun createTransaction(
      amount: BigDecimal,
      currency: String,
      transactionBuilder: TransactionBuilder,
      origin: String?,
      returnUrl: String,
  ): Single<GooglePayWebTransaction> {
    return createGooglePayWebTransactionUseCase(
            value = (amount.toString()),
            currency = currency,
            transactionBuilder = transactionBuilder,
            origin = origin,
            method = PaymentType.GOOGLEPAY_WEB.subTypes[0],
            returnUrl = returnUrl,
        )
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          when (it?.validity) {
            GooglePayWebTransaction.GooglePayWebValidityState.COMPLETED -> {
              getSuccessBundle(it.hash, null, it.uid, transactionBuilder)
            }
            GooglePayWebTransaction.GooglePayWebValidityState.PENDING -> {}
            GooglePayWebTransaction.GooglePayWebValidityState.ERROR -> {
              Log.d(TAG, "GooglePayWeb transaction error")
              sendPaymentErrorEvent(
                  errorMessage = "GooglePayWeb transaction error.",
                  transactionBuilder = transactionBuilder)
              _state.postValue(State.Error(R.string.purchase_error_google_pay))
            }
            null -> {
              Log.d(TAG, "GooglePayWeb transaction error")
              sendPaymentErrorEvent(
                  errorMessage = "GooglePayWeb transaction error.",
                  transactionBuilder = transactionBuilder)
              _state.postValue(State.Error(R.string.purchase_error_google_pay))
            }
          }
        }
  }

  private fun waitForSuccess(
      uid: String?,
      transactionBuilder: TransactionBuilder,
      wasNonSuccess: Boolean = false
  ) {
    val disposableSuccessCheck =
        waitForSuccessGooglePayWebUseCase(uid ?: "")
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .subscribe(
                {
                  when (it.status) {
                    COMPLETED -> {
                      getSuccessBundle(it.hash, null, it.uid, transactionBuilder)
                    }
                    FAILED,
                    FRAUD,
                    CANCELED,
                    INVALID_TRANSACTION -> {
                      Log.d(TAG, "Error on transaction on Settled transaction polling")
                      sendPaymentErrorEvent(
                          errorMessage =
                              "Error on transaction on Settled transaction polling ${it.status.name}",
                          transactionBuilder = transactionBuilder)
                      _state.postValue(State.Error(R.string.unknown_error))
                    }
                    else -> {
                      /* pending */
                    }
                  }
                },
                {
                  Log.d(TAG, "Error on Settled transaction polling")
                  sendPaymentErrorEvent(
                      errorMessage = "Error on Settled transaction polling",
                      transactionBuilder = transactionBuilder)
                })
    // disposes the check after x seconds
    viewModelScope.launch {
      delay(GooglePayConst.GOOGLE_PAY_TIMEOUT)
      try {
        if (state.value !is State.SuccessPurchase && wasNonSuccess)
            _state.postValue(State.Error(R.string.purchase_error_google_pay))
        disposableSuccessCheck.dispose()
      } catch (_: Exception) {}
    }
  }

  fun processGooglePayResult(transactionBuilder: TransactionBuilder) {
    if (isFirstRun) {
      isFirstRun = false
    } else {
      if (runningCustomTab) {
        runningCustomTab = false
        val result = getGooglePayResultUseCase()
        when (result) {
          GooglePayResult.SUCCESS.key -> {
            waitForSuccess(uid, transactionBuilder)
          }
          GooglePayResult.ERROR.key -> {
            sendPaymentErrorEvent("", "Error received from Web.", transactionBuilder)
            _state.postValue(State.Error(R.string.purchase_error_google_pay))
          }
          GooglePayResult.CANCEL.key -> {
            waitForSuccess(uid, transactionBuilder, true)
          }
          else -> {
            waitForSuccess(uid, transactionBuilder, true)
          }
        }
      }
    }
  }

  fun openUrlCustomTab(context: Context, url: String) {
    if (runningCustomTab) return
    runningCustomTab = true
    val customTabsBuilder = CustomTabsIntent.Builder().build()
    customTabsBuilder.intent.setPackage(GooglePayWebFragment.CHROME_PACKAGE_NAME)
    customTabsBuilder.launchUrl(context, Uri.parse(url))
  }

  fun getSuccessBundle(
      hash: String?,
      orderReference: String?,
      purchaseUid: String?,
      transactionBuilder: TransactionBuilder
  ) {
    inAppPurchaseInteractor.savePreSelectedPaymentMethod(
        PaymentMethodsView.PaymentMethodId.GOOGLEPAY_WEB.id)
    sendPaymentSuccessEvent(transactionBuilder, purchaseUid ?: "")
    createSuccessBundleUseCase(
            transactionBuilder.type,
            transactionBuilder.domain,
            transactionBuilder.skuId,
            purchaseUid,
            orderReference,
            hash,
            networkScheduler)
        .doOnSuccess {
          sendPaymentEndEvents(transactionBuilder)
          _state.postValue(State.SuccessPurchase(it.bundle))
        }
        .subscribeOn(viewScheduler)
        .observeOn(viewScheduler)
        .doOnError { _state.postValue(State.Error(R.string.unknown_error)) }
        .subscribe()
  }

  private fun sendPaymentConfirmationEvent(transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(
        Single.just(transactionBuilder)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .subscribe { it ->
              analytics.sendPaymentConfirmationEvent(
                  it.domain,
                  it.skuId,
                  it.amount().toString(),
                  BillingAnalytics.PAYMENT_METHOD_GOOGLE_PAY_WEB,
                  it.type,
                  BillingAnalytics.ACTION_BUY)
            })
  }

  private fun sendPaymentEndEvents(transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(
        Single.just(transactionBuilder)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .subscribe { it ->
              stopTimingForPurchaseEvent(true)
              analytics.sendPaymentEvent(
                  it.domain,
                  it.skuId,
                  it.amount().toString(),
                  BillingAnalytics.PAYMENT_METHOD_GOOGLE_PAY_WEB,
                  it.type)
            })
    compositeDisposable.add(
        Single.just(transactionBuilder)
            .observeOn(networkScheduler)
            .doOnSuccess {
              analytics.sendRevenueEvent(
                  adyenPaymentInteractor
                      .convertToFiat(
                          it.amount().toDouble(), BillingAnalytics.EVENT_REVENUE_CURRENCY)
                      .subscribeOn(networkScheduler)
                      .observeOn(viewScheduler)
                      .blockingGet()
                      .amount
                      .setScale(2, BigDecimal.ROUND_UP)
                      .toString())
            }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun sendPaymentSuccessEvent(transactionBuilder: TransactionBuilder, txId: String) {
    compositeDisposable.add(
        Single.just(transactionBuilder)
            .observeOn(networkScheduler)
            .doOnSuccess { transaction ->
              analytics.sendPaymentSuccessEvent(
                  packageName = transactionBuilder.domain,
                  skuDetails = transaction.skuId,
                  value = transaction.amount().toString(),
                  purchaseDetails = BillingAnalytics.PAYMENT_METHOD_GOOGLE_PAY_WEB,
                  transactionType = transaction.type,
                  txId = txId,
                  valueUsd = transaction.amountUsd.toString())
            }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun sendPaymentErrorEvent(
      errorCode: String? = null,
      errorMessage: String?,
      transactionBuilder: TransactionBuilder
  ) {
    compositeDisposable.add(
        Single.just(transactionBuilder)
            .observeOn(networkScheduler)
            .doOnSuccess { transaction ->
              stopTimingForPurchaseEvent(false)
              analytics.sendPaymentErrorWithDetailsAndRiskEvent(
                  transaction.domain,
                  transaction.skuId,
                  transaction.amount().toString(),
                  BillingAnalytics.PAYMENT_METHOD_GOOGLE_PAY_WEB,
                  transaction.type,
                  errorCode ?: "",
                  errorMessage ?: "",
                  "")
            }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun stopTimingForPurchaseEvent(success: Boolean) {
    paymentAnalytics.stopTimingForPurchaseEvent(
        PaymentMethodsAnalytics.PAYMENT_METHOD_GOOGLEPAY_WEB, success, false)
  }

  fun showSupport(gamificationLevel: Int) {
    compositeDisposable.add(
        supportInteractor.showSupport(gamificationLevel).subscribe({}, { it.printStackTrace() }))
  }

  fun handleBack(backEvent: Observable<Any>) {
    compositeDisposable.add(
        backEvent
            .observeOn(networkScheduler)
            .doOnNext { _state.postValue(State.GooglePayBack) }
            .subscribe({}, { it.printStackTrace() }))
  }

  companion object {
    private val TAG = GooglePayWebViewModel::class.java.simpleName
  }
}
