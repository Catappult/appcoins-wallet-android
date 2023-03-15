package com.asfoundation.wallet.billing.paypal

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.asf.wallet.R
import com.appcoins.wallet.ui.arch.RxSchedulers
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.billing.paypal.models.PaypalTransaction
import com.asfoundation.wallet.billing.paypal.usecases.*
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.appcoins.wallet.core.utils.common.toSingleEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class PayPalIABViewModel @Inject constructor(
  private val createPaypalTransactionUseCase: CreatePaypalTransactionUseCase,
  private val createPaypalTokenUseCase: CreatePaypalTokenUseCase,
  private val createPaypalAgreementUseCase: CreatePaypalAgreementUseCase,
  private val waitForSuccessPaypalUseCase: WaitForSuccessPaypalUseCase,
  private val createSuccessBundleUseCase: CreateSuccessBundleUseCase,
  private val cancelPaypalTokenUseCase: CancelPaypalTokenUseCase,
  private val adyenPaymentInteractor: AdyenPaymentInteractor,
  private val supportInteractor: SupportInteractor,
  rxSchedulers: RxSchedulers,
  private val analytics: BillingAnalytics,
  private val paymentAnalytics: PaymentMethodsAnalytics
) : ViewModel() {

  sealed class State {
    object Start : State()
    data class Error(val stringRes: Int) : State()
    data class WebViewAuthentication(val url: String) : State()
    data class SuccessPurchase(val bundle: Bundle) : State()
    object TokenCanceled : State()
  }

  private val _state = MutableLiveData<State>(State.Start)
  val state = _state.toSingleEvent()

  private var compositeDisposable: CompositeDisposable = CompositeDisposable()

  private var authenticatedToken: String? = null

  val networkScheduler = rxSchedulers.io
  val viewScheduler = rxSchedulers.main

  fun attemptTransaction(
    createTokenIfNeeded: Boolean = true, amount: BigDecimal, currency: String,
    transactionBuilder: TransactionBuilder, origin: String?
  ) {
    compositeDisposable.add(
      createPaypalTransactionUseCase(
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
            PaypalTransaction.PaypalValidityState.COMPLETED -> {
              Log.d(TAG, "Successful Paypal payment ")
              getSuccessBundle(it.hash, null, it.uid, transactionBuilder)
            }
            PaypalTransaction.PaypalValidityState.NO_BILLING_AGREEMENT -> {
              Log.d(TAG, "No billing agreement. Create new token? $createTokenIfNeeded ")
              if (createTokenIfNeeded) {
                createToken(transactionBuilder)
              } else {
                Log.d(TAG, "No paypal billing agreement")
                sendPaymentErrorEvent("No paypal billing agreement", transactionBuilder)
                _state.postValue(State.Error(R.string.unknown_error))
              }
            }
            PaypalTransaction.PaypalValidityState.PENDING -> {
              waitForSuccess(it.hash, it.uid, transactionBuilder)
            }
            PaypalTransaction.PaypalValidityState.ERROR -> {
              Log.d(TAG, "Paypal transaction error")
              sendPaymentErrorEvent("Paypal transaction error.", transactionBuilder)
              _state.postValue(State.Error(R.string.unknown_error))
            }
            null -> {
              Log.d(TAG, "Paypal transaction error")
              sendPaymentErrorEvent("Paypal transaction error.", transactionBuilder)
              _state.postValue(State.Error(R.string.unknown_error))
            }
          }
        }
        .subscribe({}, {
          Log.d(TAG, it.toString())
          sendPaymentErrorEvent("Paypal transaction error.", transactionBuilder)
          _state.postValue(State.Error(R.string.unknown_error))
        })
    )
  }

  fun createToken(transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(
      createPaypalTokenUseCase()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          Log.d(TAG, "Successful Token creation ")  //TODO event
          authenticatedToken = it.token
          _state.postValue(State.WebViewAuthentication(it.redirect.url))
        }
        .subscribe({}, {
          Log.d(TAG, it.toString())
          sendPaymentErrorEvent("Error on token creation", transactionBuilder)
          _state.postValue(State.Error(R.string.unknown_error))
        })
    )
  }

  fun startBillingAgreement(
    amount: BigDecimal, currency: String,
    transactionBuilder: TransactionBuilder, origin: String?
  ) {
    authenticatedToken?.let { authenticatedToken ->
      compositeDisposable.add(
        createPaypalAgreementUseCase(authenticatedToken)
          .subscribeOn(networkScheduler)
          .observeOn(viewScheduler)
          .doOnSuccess {
            Log.d(TAG, "Successful Agreement creation: ${it.uid}")
            // after creating the billing agreement, don't create a new token if it fails
            attemptTransaction(
              createTokenIfNeeded = false,
              amount,
              currency,
              transactionBuilder,
              origin
            )
          }
          .subscribe({}, {
            Log.d(TAG, it.toString())
            sendPaymentErrorEvent("Error on billing agreement creation", transactionBuilder)
            _state.postValue(State.Error(R.string.unknown_error))
          })
      )
    }
  }

  fun cancelToken() {
    authenticatedToken?.let {
      cancelPaypalTokenUseCase.invoke(it)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .subscribe {
          _state.postValue(State.TokenCanceled)
        }
    }
  }

  private fun waitForSuccess(hash: String?, uid: String?, transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(
      waitForSuccessPaypalUseCase(uid ?: "")
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .subscribe(
          {
            when (it.status) {
              PaymentModel.Status.COMPLETED -> {
                Log.d(TAG, "Settled transaction polling completed")
                getSuccessBundle(it.hash, null, it.uid, transactionBuilder)
              }
              PaymentModel.Status.FAILED, PaymentModel.Status.FRAUD, PaymentModel.Status.CANCELED,
              PaymentModel.Status.INVALID_TRANSACTION -> {
                Log.d(TAG, "Error on transaction on Settled transaction polling")
                sendPaymentErrorEvent(
                  "Error on transaction on Settled transaction polling ${it.status.name}",
                  transactionBuilder
                )
                _state.postValue(State.Error(R.string.unknown_error))
              }
              else -> { /* pending */ }
            }
          },
          {
            Log.d(TAG, "Error on Settled transaction polling")
            sendPaymentErrorEvent("Error on Settled transaction polling", transactionBuilder)
          })
    )
  }

  fun getSuccessBundle(
    hash: String?,
    orderReference: String?,
    purchaseUid: String?,
    transactionBuilder: TransactionBuilder
  ) {
    sendPaymentSuccessEvent(transactionBuilder)
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
        // TODO event
        _state.postValue(State.Error(R.string.unknown_error))
      }
      .subscribe()
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
          BillingAnalytics.PAYMENT_METHOD_PAYPALV2,
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

  private fun sendPaymentSuccessEvent(transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(Single.just(transactionBuilder)
      .observeOn(networkScheduler)
      .doOnSuccess { transaction ->
        analytics.sendPaymentSuccessEvent(
          transactionBuilder.domain,
          transaction.skuId,
          transaction.amount().toString(),
          BillingAnalytics.PAYMENT_METHOD_PAYPALV2,
          transaction.type
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun sendPaymentErrorEvent(
    refusalReason: String?,
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
          BillingAnalytics.PAYMENT_METHOD_PAYPALV2,
          transaction.type,
          "",
          refusalReason ?: "",
          ""
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun stopTimingForPurchaseEvent(success: Boolean) {
    paymentAnalytics.stopTimingForPurchaseEvent(
      PaymentMethodsAnalytics.PAYMENT_METHOD_PP_V2,
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
    private val TAG = PayPalIABViewModel::class.java.simpleName
  }

}
