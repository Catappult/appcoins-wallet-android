package com.asfoundation.wallet.billing.wallet_one

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.CANCELED
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.COMPLETED
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.FAILED
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.FRAUD
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.INVALID_TRANSACTION
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.network.microservices.model.WalletOneTransaction
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.android_common.toSingleEvent
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.googlepay.usecases.WaitForSuccessUseCase
import com.asfoundation.wallet.billing.paypal.usecases.CreateSuccessBundleUseCase
import com.asfoundation.wallet.billing.wallet_one.models.WalletOneConst
import com.asfoundation.wallet.billing.wallet_one.usecases.CreateWalletOneTransactionUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import com.wallet.appcoins.feature.support.data.SupportInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class WalletOneViewModel @Inject constructor(
  private val createWalletOneTransactionUseCase: CreateWalletOneTransactionUseCase,
  private val waitForSuccessWalletOneUseCase: WaitForSuccessUseCase,
  private val createSuccessBundleUseCase: CreateSuccessBundleUseCase,
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
    data class WebAuthentication(val htmlData: String) : State()
    data class SuccessPurchase(val bundle: Bundle) : State()
    object WalletOneBack : State()
  }

  private val _state = MutableLiveData<State>(State.Start)
  val state = _state.toSingleEvent()

  private var compositeDisposable: CompositeDisposable = CompositeDisposable()

  val networkScheduler = rxSchedulers.io
  val viewScheduler = rxSchedulers.main

  var uid: String? = null
  var shouldStartPayment = true

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
        createWalletOneTransactionUseCase(
          value = (amount.toString()),
          currency = currency,
          transactionBuilder = transactionBuilder,
          origin = origin,
          method = PaymentType.WALLET_ONE.subTypes[0],
          successUrl = WalletOneReturnSchemas.SUCCESS.schema,
          failUrl = WalletOneReturnSchemas.ERROR.schema,
        )
          .subscribeOn(networkScheduler)
          .observeOn(viewScheduler)
          .map { transaction ->
            if (
              transaction?.validity == null ||
              transaction?.validity == WalletOneTransaction.WalletOneValidityState.ERROR
            ) {
              Log.d(TAG, "WalletOne transaction error. Error starting transaction")
              sendPaymentErrorEvent(
                errorMessage = "WalletOne transaction error. Error starting transaction",
                transactionBuilder = transactionBuilder
              )
              _state.postValue(State.Error(R.string.purchase_error_google_pay)) //TODO string
            } else {
              uid = transaction.uid
              Log.d("htmlData", transaction.htmlData ?: "null")
              _state.postValue(State.WebAuthentication(transaction.htmlData ?: ""))
            }
          }.subscribe({}, {
            Log.d(TAG, it.toString())
            sendPaymentErrorEvent(
              errorMessage = "WalletOne transaction error. Error starting transaction",
              transactionBuilder = transactionBuilder
            )
            _state.postValue(State.Error(R.string.purchase_error_google_pay)) //TODO string
          })
      )
    }
  }

  fun waitForSuccess(
    uid: String?,
    transactionBuilder: TransactionBuilder,
    wasNonSuccess: Boolean = false
  ) {
    val disposableSuccessCheck = waitForSuccessWalletOneUseCase(uid ?: "")
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .subscribe({
        when (it.status) {
          COMPLETED -> {
            getSuccessBundle(it.hash, null, it.uid, transactionBuilder)
          }

          FAILED, FRAUD, CANCELED, INVALID_TRANSACTION -> {
            Log.d(TAG, "Error on transaction on Settled transaction polling")
            sendPaymentErrorEvent(
              errorMessage = "Error on transaction on Settled transaction polling ${it.status.name}",
              transactionBuilder = transactionBuilder
            )
            _state.postValue(State.Error(R.string.unknown_error))
          }

          else -> { /* pending */
          }
        }
      }, {
        Log.d(TAG, "Error on Settled transaction polling")
        sendPaymentErrorEvent(
          errorMessage = "Error on Settled transaction polling",
          transactionBuilder = transactionBuilder
        )
      })
    // disposes the check after x seconds
    viewModelScope.launch {
      delay(WalletOneConst.WALLET_ONE_TIMEOUT)
      try {
        if (state.value !is State.SuccessPurchase && wasNonSuccess)
          _state.postValue(State.Error(R.string.purchase_error_google_pay)) //TODO string
        disposableSuccessCheck.dispose()
      } catch (_: Exception) {
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
      PaymentMethodsView.PaymentMethodId.WALLET_ONE.id
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
    ).doOnSuccess {
      sendPaymentEndEvents(transactionBuilder)
      _state.postValue(State.SuccessPurchase(it.bundle))
    }.subscribeOn(viewScheduler).observeOn(viewScheduler).doOnError {
      _state.postValue(State.Error(R.string.unknown_error))
    }.subscribe()
  }

  private fun sendPaymentConfirmationEvent(transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(Single.just(transactionBuilder).subscribeOn(networkScheduler)
      .observeOn(viewScheduler).subscribe { it ->
        analytics.sendPaymentConfirmationEvent(
          it.domain,
          it.skuId,
          it.amount().toString(),
          BillingAnalytics.PAYMENT_METHOD_GOOGLE_PAY_WEB,
          it.type,
          BillingAnalytics.ACTION_BUY
        )
      })
  }

  private fun sendPaymentEndEvents(transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(Single.just(transactionBuilder).subscribeOn(networkScheduler)
      .observeOn(viewScheduler).subscribe { it ->
        stopTimingForPurchaseEvent(true)
        analytics.sendPaymentEvent(
          it.domain,
          it.skuId,
          it.amount().toString(),
          BillingAnalytics.PAYMENT_METHOD_GOOGLE_PAY_WEB,
          it.type
        )
      })
    compositeDisposable.add(Single.just(transactionBuilder).observeOn(networkScheduler)
      .doOnSuccess {
        analytics.sendRevenueEvent(
          adyenPaymentInteractor.convertToFiat(
            it.amount().toDouble(), BillingAnalytics.EVENT_REVENUE_CURRENCY
          ).subscribeOn(networkScheduler).observeOn(viewScheduler).blockingGet().amount.setScale(
            2,
            BigDecimal.ROUND_UP
          ).toString()
        )
      }.subscribe({}, { it.printStackTrace() })
    )
  }

  private fun sendPaymentSuccessEvent(transactionBuilder: TransactionBuilder, txId: String) {
    compositeDisposable.add(Single.just(transactionBuilder).observeOn(networkScheduler)
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
      }.subscribe({}, { it.printStackTrace() })
    )
  }

  private fun sendPaymentErrorEvent(
    errorCode: String? = null, errorMessage: String?, transactionBuilder: TransactionBuilder
  ) {
    compositeDisposable.add(Single.just(transactionBuilder).observeOn(networkScheduler)
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
      }.subscribe({}, { it.printStackTrace() })
    )
  }

  private fun stopTimingForPurchaseEvent(success: Boolean) {
    paymentAnalytics.stopTimingForPurchaseEvent(
      PaymentMethodsAnalytics.PAYMENT_METHOD_WALLET_ONE, success, false
    )
  }

  fun showSupport(gamificationLevel: Int) {
    compositeDisposable.add(
      supportInteractor.showSupport(gamificationLevel).subscribe({}, { it.printStackTrace() })
    )
  }

  fun handleBack(backEvent: Observable<Any>) {
    compositeDisposable.add(backEvent
      .observeOn(networkScheduler)
      .doOnNext { _state.postValue(State.WalletOneBack) }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  companion object {
    private val TAG = WalletOneViewModel::class.java.simpleName
  }

}
