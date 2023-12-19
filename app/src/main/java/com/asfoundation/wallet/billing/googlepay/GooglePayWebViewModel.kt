package com.asfoundation.wallet.billing.googlepay

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.asf.wallet.R
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.network.microservices.model.GooglePayWebTransaction
import com.asfoundation.wallet.billing.googlepay.usecases.*
import com.asfoundation.wallet.entity.TransactionBuilder
import com.wallet.appcoins.feature.support.data.SupportInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.appcoins.wallet.core.utils.android_common.toSingleEvent
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import com.asfoundation.wallet.ui.iab.share.ShareLinkInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class GooglePayWebViewModel @Inject constructor(
  private val createGooglePayWebTransactionUseCase: CreateGooglePayWebTransactionUseCase,
  private val createGooglePayWebTokenUseCase: CreateGooglePayWebTokenUseCase,
  private val createGooglePayWebAgreementUseCase: CreateGooglePayWebAgreementUseCase,
  private val waitForSuccessGooglePayWebUseCase: WaitForSuccessGooglePayWebUseCase,
  private val createSuccessBundleUseCase: CreateSuccessBundleUseCase,
  private val cancelGooglePayWebTokenUseCase: CancelGooglePayWebTokenUseCase,
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
    object TokenCanceled : State()
  }

  private val _state = MutableLiveData<State>(State.Start)
  val state = _state.toSingleEvent()

  private var compositeDisposable: CompositeDisposable = CompositeDisposable()

  private var authenticatedToken: String? = null

  val networkScheduler = rxSchedulers.io
  val viewScheduler = rxSchedulers.main

  fun startPayment(
    createTokenIfNeeded: Boolean = true, amount: BigDecimal, currency: String,
    transactionBuilder: TransactionBuilder, origin: String?
  ) {
    sendPaymentConfirmationEvent(transactionBuilder)
    attemptTransaction(
      createTokenIfNeeded = createTokenIfNeeded,
      amount = amount,
      currency = currency,
      transactionBuilder = transactionBuilder,
      origin = origin
    )
  }

  fun attemptTransaction(
    createTokenIfNeeded: Boolean = true, amount: BigDecimal, currency: String,
    transactionBuilder: TransactionBuilder, origin: String?
  ) {
    compositeDisposable.add(
      createGooglePayWebTransactionUseCase(
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
            GooglePayWebTransaction.GooglePayWebValidityState.COMPLETED -> {
              getSuccessBundle(it.hash, null, it.uid, transactionBuilder)
            }
            GooglePayWebTransaction.GooglePayWebValidityState.NO_BILLING_AGREEMENT -> {
              Log.d(TAG, "No billing agreement. Create new token? $createTokenIfNeeded ")
              if (createTokenIfNeeded) {
                createToken(transactionBuilder)
              } else {
                Log.d(TAG, "No googlePayWeb billing agreement")
                sendPaymentErrorEvent(it.errorCode, it.errorMessage, transactionBuilder)
                _state.postValue(State.Error(R.string.purchase_error_googlepay))
              }
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
              _state.postValue(State.Error(R.string.purchase_error_googlepay))
            }
            null -> {
              Log.d(TAG, "GooglePayWeb transaction error")
              sendPaymentErrorEvent(
                errorMessage = "GooglePayWeb transaction error.",
                transactionBuilder = transactionBuilder
              )
              _state.postValue(State.Error(R.string.purchase_error_googlepay))
            }
          }
        }
        .subscribe({}, {
          Log.d(TAG, it.toString())
          sendPaymentErrorEvent(
            errorMessage = "GooglePayWeb transaction error.",
            transactionBuilder = transactionBuilder
          )
          _state.postValue(State.Error(R.string.purchase_error_googlepay))
        })
    )
  }

  fun createToken(transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(
      createGooglePayWebTokenUseCase()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          authenticatedToken = it.token
          _state.postValue(State.WebViewAuthentication(it.redirect.url))
        }
        .subscribe({}, {
          Log.d(TAG, it.toString())
          sendPaymentErrorEvent(
            errorMessage = "Error on token creation",
            transactionBuilder = transactionBuilder
          )
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
        createGooglePayWebAgreementUseCase(authenticatedToken)
          .subscribeOn(networkScheduler)
          .observeOn(viewScheduler)
          .doOnSuccess {
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
            sendPaymentErrorEvent(
              errorMessage = "Error on billing agreement creation",
              transactionBuilder = transactionBuilder
            )
            _state.postValue(State.Error(R.string.unknown_error))
          })
      )
    }
  }

  fun cancelToken() {
    authenticatedToken?.let {
      cancelGooglePayWebTokenUseCase.invoke(it)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .subscribe {
          _state.postValue(State.TokenCanceled)
        }
    }
  }

  private fun waitForSuccess(hash: String?, uid: String?, transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(
      waitForSuccessGooglePayWebUseCase(uid ?: "")
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
              else -> { /* pending */
              }
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
    inAppPurchaseInteractor.savePreSelectedPaymentMethod(
      PaymentMethodsView.PaymentMethodId.PAYPAL_V2.id
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
          BillingAnalytics.PAYMENT_METHOD_PAYPALV2,
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

  private fun sendPaymentSuccessEvent(transactionBuilder: TransactionBuilder, txId: String) {
    compositeDisposable.add(Single.just(transactionBuilder)
      .observeOn(networkScheduler)
      .doOnSuccess { transaction ->
        analytics.sendPaymentSuccessEvent(
          packageName = transactionBuilder.domain,
          skuDetails = transaction.skuId,
          value = transaction.amount().toString(),
          purchaseDetails = BillingAnalytics.PAYMENT_METHOD_PAYPALV2,
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
          BillingAnalytics.PAYMENT_METHOD_PAYPALV2,
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
    private val TAG = GooglePayWebViewModel::class.java.simpleName
  }

}
