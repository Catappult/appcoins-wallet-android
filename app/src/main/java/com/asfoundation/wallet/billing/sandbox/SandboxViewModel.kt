package com.asfoundation.wallet.billing.sandbox

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.asf.wallet.R
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.network.microservices.model.SandboxTransaction
import com.asfoundation.wallet.entity.TransactionBuilder
import com.wallet.appcoins.feature.support.data.SupportInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.appcoins.wallet.core.utils.android_common.toSingleEvent
import com.asfoundation.wallet.billing.paypal.usecases.CreateSuccessBundleUseCase
import com.asfoundation.wallet.billing.sandbox.usecases.CreateSandboxTransactionUseCase
import com.asfoundation.wallet.billing.sandbox.usecases.WaitForSuccessSandboxUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class SandboxViewModel @Inject constructor(
  private val createSandboxTransactionUseCase: CreateSandboxTransactionUseCase,
  private val createSuccessBundleUseCase: CreateSuccessBundleUseCase,
  private val waitForSuccessSandboxUseCase: WaitForSuccessSandboxUseCase,
  private val adyenPaymentInteractor: AdyenPaymentInteractor,
  private val supportInteractor: SupportInteractor,
  rxSchedulers: RxSchedulers,
  private val analytics: BillingAnalytics,
  private val paymentAnalytics: PaymentMethodsAnalytics
) : ViewModel() {

  sealed class State {
    object Start : State()
    data class Error(val stringRes: Int) : State()
    data class SuccessPurchase(val bundle: Bundle) : State()
  }

  private val _state = MutableLiveData<State>(State.Start)
  val state = _state.toSingleEvent()

  private var compositeDisposable: CompositeDisposable = CompositeDisposable()

  private var authenticatedToken: String? = null

  val networkScheduler = rxSchedulers.io
  val viewScheduler = rxSchedulers.main

  fun startPayment(
    amount: BigDecimal, currency: String,
    transactionBuilder: TransactionBuilder, origin: String?
  ) {
    sendPaymentConfirmationEvent(transactionBuilder)
    compositeDisposable.add(
      createSandboxTransactionUseCase(
        value = (amount.toString()),
        currency = currency,
        reference = transactionBuilder.orderReference,
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
        .doOnSuccess {
          when (it?.validity) {
            SandboxTransaction.SandboxValidityState.COMPLETED -> {
              getSuccessBundle(it.hash, null, it.uid, transactionBuilder)
            }
            SandboxTransaction.SandboxValidityState.PENDING -> {
              waitForSuccess(it.hash, it.uid, transactionBuilder)
            }
            SandboxTransaction.SandboxValidityState.ERROR -> {
              Log.d(TAG, "Sandbox transaction error")
              sendPaymentErrorEvent(
                errorMessage = "Sandbox transaction error.",
                transactionBuilder = transactionBuilder
              )
              _state.postValue(State.Error(R.string.unknown_error))
            }
            null -> {
              Log.d(TAG, "Sandbox transaction error")
              sendPaymentErrorEvent(
                errorMessage = "Sandbox transaction error.",
                transactionBuilder = transactionBuilder
              )
              _state.postValue(State.Error(R.string.unknown_error))
            }
          }
        }
        .subscribe({}, {
          Log.d(TAG, it.toString())
          sendPaymentErrorEvent(
            errorMessage = "Sandbox transaction error.",
            transactionBuilder = transactionBuilder
          )
          _state.postValue(State.Error(R.string.unknown_error))
        })
    )
  }

  private fun waitForSuccess(hash: String?, uid: String?, transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(
      waitForSuccessSandboxUseCase(uid ?: "")
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .subscribe(
          {
            when (it.status) {
              PaymentModel.Status.COMPLETED -> {
                getSuccessBundle(it.hash, null, it.uid, transactionBuilder)
              }
              PaymentModel.Status.FAILED, PaymentModel.Status.FRAUD, PaymentModel.Status.CANCELED,
              PaymentModel.Status.INVALID_TRANSACTION -> {
                Log.d(TAG, "Error on transaction on Settled transaction polling")
                sendPaymentErrorEvent(
                  errorMessage = "Error on transaction on Settled transaction polling ${it.status.name}",
                  transactionBuilder = transactionBuilder
                )
                _state.postValue(State.Error(R.string.unknown_error))
              }
              else -> { /* pending */ }
            }
          },
          {
            Log.d(TAG, "Error on Settled transaction polling")
            sendPaymentErrorEvent(
              errorMessage = "Error on Settled transaction polling",
              transactionBuilder = transactionBuilder
            )
          })
    )
  }

  fun getSuccessBundle(
    hash: String?,
    orderReference: String?,
    purchaseUid: String?,
    transactionBuilder: TransactionBuilder
  ) {
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
//        sendRevenueEvent(transactionBuilder)
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
          BillingAnalytics.PAYMENT_METHOD_SANDBOX,
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
          BillingAnalytics.PAYMENT_METHOD_SANDBOX,
          it.type
        )
      }
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
          purchaseDetails = BillingAnalytics.PAYMENT_METHOD_SANDBOX,
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
          BillingAnalytics.PAYMENT_METHOD_SANDBOX,
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
      PaymentMethodsAnalytics.PAYMENT_METHOD_SANDBOX,
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
    private val TAG = SandboxViewModel::class.java.simpleName
  }

}
