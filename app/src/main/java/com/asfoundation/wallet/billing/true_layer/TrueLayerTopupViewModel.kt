package com.asfoundation.wallet.billing.true_layer

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.CANCELED
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.COMPLETED
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.FAILED
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.FRAUD
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.INVALID_TRANSACTION
import com.appcoins.wallet.core.network.microservices.model.TrueLayerTransaction
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.android_common.toSingleEvent
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.googlepay.usecases.WaitForSuccessUseCase
import com.asfoundation.wallet.billing.true_layer.usecases.CreateTrueLayerTransactionTopupUseCase
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.wallet.appcoins.feature.support.data.SupportInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrueLayerTopupViewModel @Inject constructor(
  private val createTrueLayerTransactionTopupUseCase: CreateTrueLayerTransactionTopupUseCase,
  private val waitForSuccessUseCase: WaitForSuccessUseCase,
  private val billingMessagesMapper: BillingMessagesMapper,
  private val supportInteractor: SupportInteractor,
  private val topUpAnalytics: TopUpAnalytics,
  rxSchedulers: RxSchedulers
) : ViewModel() {

  sealed class State {
    object Start : State()
    data class Error(val stringRes: Int) : State()
    data class LaunchTrueLayerSDK(val paymentId: String, val resourceToken: String) : State()
    data class SuccessPurchase(val hash: String?, val uid: String?) : State()
    object TrueLayerBack : State()
  }

  val _state = MutableLiveData<State>(State.Start)
  val state = _state.toSingleEvent()

  private var compositeDisposable: CompositeDisposable = CompositeDisposable()

  val networkScheduler = rxSchedulers.io
  val viewScheduler = rxSchedulers.main

  var uid: String? = null
  var shouldStartPayment = true

  fun startPayment(
    amount: String,
    currency: String,
  ) {
    if (shouldStartPayment) {
      shouldStartPayment = false
      topUpAnalytics.sendConfirmationEvent(
        amount.toDouble(),
        "top_up",
        PaymentMethodsAnalytics.PAYMENT_METHOD_TRUE_LAYER
      )
      compositeDisposable.add(
        createTrueLayerTransactionTopupUseCase(
          value = amount,
          currency = currency,
          method = PaymentType.TRUE_LAYER.subTypes[0],
        )
          .subscribeOn(networkScheduler)
          .observeOn(viewScheduler)
          .map { transaction ->
            if (
              transaction?.validity == null ||
              transaction?.validity == TrueLayerTransaction.TrueLayerValidityState.ERROR
            ) {
              Log.d(TAG, "TrueLayer transaction error. Error starting transaction")
              topUpAnalytics.sendTrueLayerErrorEvent(
                errorCode = "TRANSACTION_START_ERROR",
                errorDetails = "TrueLayer transaction error. Error starting transaction",
              )
              _state.postValue(State.Error(R.string.purchase_error_open_banking_wallet_generic))
            } else {
              uid = transaction.uid
              Log.d("paymentId", transaction.paymentId ?: "null")
              _state.postValue(
                State.LaunchTrueLayerSDK(
                  paymentId = transaction.paymentId ?: "",
                  resourceToken = transaction.resourceToken ?: ""
                )
              )
            }
          }.subscribe({}, {
            Log.d(TAG, it.toString())
            topUpAnalytics.sendTrueLayerErrorEvent(errorDetails = "TrueLayer transaction error")
            _state.postValue(State.Error(R.string.purchase_error_open_banking_wallet_generic))
          })
      )
    }
  }

  fun waitForSuccess(
    uid: String?,
    amount: String,
    wasNonSuccess: Boolean = false
  ) {
    val disposableSuccessCheck = waitForSuccessUseCase(uid ?: "")
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .subscribe(
        {
          when (it.status) {
            COMPLETED -> {
              topUpAnalytics.sendSuccessEvent(
                amount.toDouble(),
                PaymentMethodsAnalytics.PAYMENT_METHOD_TRUE_LAYER,
                TopUpAnalytics.STATUS_SUCCESS
              )
              _state.postValue(State.SuccessPurchase(it.hash, it.uid))
            }

            FAILED, FRAUD, CANCELED, INVALID_TRANSACTION -> {
              Log.d(TAG, "Error on transaction on Settled transaction polling")
              topUpAnalytics.sendTrueLayerErrorEvent(
                errorDetails = "Error on transaction on Settled transaction polling ${it.status.name}"
              )
              _state.postValue(State.Error(R.string.unknown_error))
            }

            else -> { /* pending */
            }
          }
        },
        {
          Log.d(TAG, "Error on Settled transaction polling")
          topUpAnalytics.sendTrueLayerErrorEvent(errorDetails = "Error on Settled transaction polling")
        })
    // disposes the check after x seconds
    viewModelScope.launch {
      delay(16000L)   //TODO remove if not needed
      try {
        if (state.value !is State.SuccessPurchase && wasNonSuccess)
          _state.postValue(State.Error(R.string.purchase_error_open_banking_wallet_generic))
        disposableSuccessCheck.dispose()
      } catch (_: Exception) {
      }
    }
  }

  fun createBundle(
    priceAmount: String, priceCurrency: String,
    fiatCurrencySymbol: String, bonus: String, pendingFinalConfirmation: Boolean
  ): Bundle {
    val bundle = billingMessagesMapper.topUpBundle(
      priceAmount, priceCurrency, bonus,
      fiatCurrencySymbol
    )
    bundle.putBoolean(BillingMessagesMapper.PENDING_FINAL_CONFIRMATION, pendingFinalConfirmation)
    return bundle
  }

  fun sendErrorEvent(message: String) {
    topUpAnalytics.sendTrueLayerErrorEvent(errorDetails = message)
  }

  fun showSupport() {
    compositeDisposable.add(
      supportInteractor.showSupport().subscribe({}, { it.printStackTrace() })
    )
  }

  companion object {
    private val TAG = TrueLayerTopupViewModel::class.java.simpleName
  }

}