package com.asfoundation.wallet.billing.googlepay

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.network.microservices.model.GooglePayWebTransaction
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.android_common.toSingleEvent
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.googlepay.models.GooglePayResult
import com.asfoundation.wallet.billing.googlepay.usecases.*
import com.asfoundation.wallet.billing.paypal.usecases.CreateSuccessBundleUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import com.wallet.appcoins.feature.support.data.SupportInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class GooglePayWebViewModel @Inject constructor(
  private val getGooglePayUrlUseCase: GetGooglePayUrlUseCase,
  private val createGooglePayWebTransactionUseCase: CreateGooglePayWebTransactionUseCase,
  private val buildGooglePayUrlUseCase: BuildGooglePayUrlUseCase,
//  private val waitForSuccessGooglePayWebUseCase: WaitForSuccessGooglePayWebUseCase, // TODO
  private val createSuccessBundleUseCase: CreateSuccessBundleUseCase,  //TODO check
  private val getGooglePayResultUseCase: GetGooglePayResultUseCase,
  private val adyenPaymentInteractor: AdyenPaymentInteractor,
  private val supportInteractor: SupportInteractor,
  private val inAppPurchaseInteractor: InAppPurchaseInteractor,
  rxSchedulers: RxSchedulers,
  private val analytics: BillingAnalytics,
  private val paymentAnalytics: PaymentMethodsAnalytics
) : ViewModel() {

  sealed class State {
    object Start : State()
    data class Error(val stringRes: Int) : State()
    data class WebViewAuthentication(val url: String) : State()
    data class SuccessPurchase(val bundle: Bundle) : State()
  }

  private val _state = MutableLiveData<State>(State.Start)
  val state = _state.toSingleEvent()

  private var compositeDisposable: CompositeDisposable = CompositeDisposable()

  private var authenticatedToken: String? = null

  val networkScheduler = rxSchedulers.io
  val viewScheduler = rxSchedulers.main

  var uid: String? = null

  fun startPayment(
    createTokenIfNeeded: Boolean = true, amount: BigDecimal, currency: String,
    transactionBuilder: TransactionBuilder, origin: String?
  ) {
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
            returnUrl = urls.returnUrl, // "https://wallet.dev.appcoins.io/app/googlepay/checkout",
          )
            .map { transaction ->
              uid = transaction.uid
              val googlePayUrl = buildGooglePayUrlUseCase(
                url = urls.url,
                sessionId = transaction.sessionId ?: "",
                sessionData = transaction.sessionData ?: "",
                isDarkMode = false,
              )
              _state.postValue(State.WebViewAuthentication(googlePayUrl))
            }
        }
        .subscribe({}, {
          Log.d(TAG, it.toString())
          sendPaymentErrorEvent(
            errorMessage = "GooglePayWeb transaction error.",
            transactionBuilder = transactionBuilder
          )
          _state.postValue(State.Error(R.string.purchase_error_paypal))  // TODO new error string
        })
    )
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
        reference = transactionBuilder.orderReference,
        origin = origin,
        packageName = transactionBuilder.domain,
        metadata = transactionBuilder.payload,
        method = PaymentType.GOOGLEPAY_WEB.subTypes[0],
        sku = transactionBuilder.skuId,
        callbackUrl = transactionBuilder.callbackUrl,
        transactionType = transactionBuilder.type,
        developerWallet = transactionBuilder.toAddress(),
        referrerUrl = transactionBuilder.referrerUrl,
        returnUrl = returnUrl,
      )
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          when (it?.validity) {
            GooglePayWebTransaction.GooglePayWebValidityState.COMPLETED -> {
              getSuccessBundle(it.hash, null, it.uid, transactionBuilder)
            }
            GooglePayWebTransaction.GooglePayWebValidityState.PENDING -> {
              waitForSuccess(it.hash, it.uid, transactionBuilder)
            }
            GooglePayWebTransaction.GooglePayWebValidityState.ERROR -> {
              Log.d(TAG, "GooglePayWeb transaction error")
              sendPaymentErrorEvent(
                errorMessage = "GooglePayWeb transaction error.",
                transactionBuilder = transactionBuilder
              )
              _state.postValue(State.Error(R.string.purchase_error_paypal))   // TODO new error string
            }
            null -> {
              Log.d(TAG, "GooglePayWeb transaction error")
              sendPaymentErrorEvent(
                errorMessage = "GooglePayWeb transaction error.",
                transactionBuilder = transactionBuilder
              )
              _state.postValue(State.Error(R.string.purchase_error_paypal))   // TODO new error string
            }
          }
        }
  }

  private fun waitForSuccess(
    hash: String?,
    uid: String?,
    transactionBuilder: TransactionBuilder
  ) {  //TODO
//    compositeDisposable.add(
//      waitForSuccessGooglePayWebUseCase(uid ?: "")
//        .subscribeOn(networkScheduler)
//        .observeOn(viewScheduler)
//        .subscribe(
//          {
//            when (it.status) {
//              PaymentModel.Status.COMPLETED -> {
//                getSuccessBundle(it.hash, null, it.uid, transactionBuilder)
//              }
//              PaymentModel.Status.FAILED,
//              PaymentModel.Status.FRAUD,
//              PaymentModel.Status.CANCELED,
//              PaymentModel.Status.INVALID_TRANSACTION -> {
//                Log.d(TAG, "Error on transaction on Settled transaction polling")
//                sendPaymentErrorEvent(
//                  errorMessage = "Error on transaction on Settled transaction polling ${it.status.name}",
//                  transactionBuilder = transactionBuilder
//                )
//                _state.postValue(State.Error(R.string.unknown_error))
//              }
//              else -> { /* pending */
//              }
//            }
//          },
//          {
//            Log.d(TAG, "Error on Settled transaction polling")
//            sendPaymentErrorEvent(
//              errorMessage = "Error on Settled transaction polling",
//              transactionBuilder = transactionBuilder
//            )
//          }
//        )
//    )
  }

  fun processGooglePayResult() {
    val result = getGooglePayResultUseCase()
    when (result) {
      GooglePayResult.SUCCESS.key -> {
        Log.d(TAG, "success")
        //TODO
      }
      GooglePayResult.CANCEL.key -> {
        Log.d(TAG, "cancel")
        //TODO
      }
      GooglePayResult.ERROR.key -> {
        Log.d(TAG, "error")
        //TODO
      }
      else -> {
        Log.d(TAG, "else")
        //TODO
      }
    }
  }

  fun getSuccessBundle(
    hash: String?,
    orderReference: String?,
    purchaseUid: String?,
    transactionBuilder: TransactionBuilder
  ) {
    inAppPurchaseInteractor.savePreSelectedPaymentMethod(
      PaymentMethodsView.PaymentMethodId.GOOGLEPAY_WEB.id
    )
    sendPaymentSuccessEvent(transactionBuilder, purchaseUid ?: "")
    createSuccessBundleUseCase(
      transactionBuilder.type,
      transactionBuilder.domain,
      transactionBuilder.skuId,
      purchaseUid,
      orderReference,
      hash,
      networkScheduler
    )
      .doOnSuccess {
        sendPaymentEvent(transactionBuilder)
        sendRevenueEvent(transactionBuilder)
        _state.postValue(State.SuccessPurchase(it.bundle))
      }
      .subscribeOn(viewScheduler)
      .observeOn(viewScheduler)
      .doOnError {
        _state.postValue(State.Error(R.string.unknown_error))
      }
      .subscribe()
  }

  private fun sendPaymentConfirmationEvent(transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(Single.just(transactionBuilder)
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .subscribe { it ->
        analytics.sendPaymentConfirmationEvent(
          it.domain,
          it.skuId,
          it.amount().toString(),
          BillingAnalytics.PAYMENT_METHOD_GOOGLE_PAY_WEB,
          it.type,
          BillingAnalytics.ACTION_BUY
        )
      }
    )
  }

  private fun sendPaymentEvent(transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(Single.just(transactionBuilder)
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .subscribe { it ->
        stopTimingForPurchaseEvent(true)
        analytics.sendPaymentEvent(
          it.domain,
          it.skuId,
          it.amount().toString(),
          BillingAnalytics.PAYMENT_METHOD_GOOGLE_PAY_WEB,
          it.type
        )
      }
    )
  }

  private fun sendRevenueEvent(transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(Single.just(transactionBuilder)
      .observeOn(networkScheduler)
      .doOnSuccess {
        analytics.sendRevenueEvent(
          adyenPaymentInteractor.convertToFiat(
            it.amount().toDouble(),
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

  private fun sendPaymentSuccessEvent(transactionBuilder: TransactionBuilder, txId: String) {
    compositeDisposable.add(Single.just(transactionBuilder)
      .observeOn(networkScheduler)
      .doOnSuccess { transaction ->
        analytics.sendPaymentSuccessEvent(
          packageName = transactionBuilder.domain,
          skuDetails = transaction.skuId,
          value = transaction.amount().toString(),
          purchaseDetails = BillingAnalytics.PAYMENT_METHOD_GOOGLE_PAY_WEB,
          transactionType = transaction.type,
          txId = txId,
          valueUsd = transaction.amountUsd.toString()
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun sendPaymentErrorEvent(
    errorCode: String? = null,
    errorMessage: String?,
    transactionBuilder: TransactionBuilder
  ) {
    compositeDisposable.add(Single.just(transactionBuilder)
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
          ""
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun stopTimingForPurchaseEvent(success: Boolean) {
    paymentAnalytics.stopTimingForPurchaseEvent(
      PaymentMethodsAnalytics.PAYMENT_METHOD_GOOGLEPAY_WEB,
      success,
      false
    )
  }

  fun showSupport(gamificationLevel: Int) {
    compositeDisposable.add(
      supportInteractor.showSupport(gamificationLevel).subscribe({}, { it.printStackTrace() })
    )
  }

  companion object {
    private val TAG = GooglePayWebViewModel::class.java.simpleName
  }

}
