package com.asfoundation.wallet.billing.paypal

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.asf.wallet.R
import com.appcoins.wallet.ui.arch.RxSchedulers
import com.asfoundation.wallet.billing.paypal.models.PaypalTransaction
import com.asfoundation.wallet.billing.paypal.usecases.*
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.util.toSingleEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@HiltViewModel
class PayPalTopupViewModel @Inject constructor(
  private val createPaypalTransactionTopupUseCase: CreatePaypalTransactionTopupUseCase,
  private val createPaypalTokenUseCase: CreatePaypalTokenUseCase,
  private val createPaypalAgreementUseCase: CreatePaypalAgreementUseCase,
  private val waitForSuccessPaypalUseCase: WaitForSuccessPaypalUseCase,
  private val cancelPaypalTokenUseCase: CancelPaypalTokenUseCase,
  private val billingMessagesMapper: BillingMessagesMapper,
  private val supportInteractor: SupportInteractor,
  private val topUpAnalytics: TopUpAnalytics,
  rxSchedulers: RxSchedulers
) : ViewModel() {

  sealed class State {
    object Start : State()
    data class Error(val stringRes: Int) : State()
    data class WebViewAuthentication(val url: String) : State()
    data class SuccessPurchase(val hash: String?, val uid: String?) : State()
    object TokenCanceled : State()
  }

  private val _state = MutableLiveData<State>(State.Start)
  val state = _state.toSingleEvent()

  private var compositeDisposable: CompositeDisposable = CompositeDisposable()

  private var authenticatedToken: String? = null

  val networkScheduler = rxSchedulers.io
  val viewScheduler = rxSchedulers.main

  fun attemptTransaction(
    createTokenIfNeeded: Boolean = true, amount: String, currency: String
  ) {
    compositeDisposable.add(
      createPaypalTransactionTopupUseCase(
        value = amount,
        currency = currency
      )
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          when (it?.validity) {
            PaypalTransaction.PaypalValidityState.COMPLETED -> {
              Log.d(TAG, "Successful Paypal payment ")
              topUpAnalytics.sendPaypalSuccessEvent(amount)
              _state.postValue(State.SuccessPurchase(it.hash, it.uid))
            }
            PaypalTransaction.PaypalValidityState.NO_BILLING_AGREEMENT -> {
              Log.d(TAG, "No billing agreement. Create new token? $createTokenIfNeeded ")
              if (createTokenIfNeeded) {
                createToken()
              } else {
                Log.d(TAG, "No paypal billing agreement")
                topUpAnalytics.sendPaypalErrorEvent("No paypal billing agreement")
                _state.postValue(State.Error(R.string.unknown_error))
              }
            }
            PaypalTransaction.PaypalValidityState.PENDING -> {
              waitForSuccess(it.hash, it.uid, amount)
            }
            PaypalTransaction.PaypalValidityState.ERROR -> {
              Log.d(TAG, "Paypal transaction error")
              topUpAnalytics.sendPaypalErrorEvent("Paypal transaction error")
              _state.postValue(State.Error(R.string.unknown_error))
            }
            null -> {
              Log.d(TAG, "Paypal transaction error")
              topUpAnalytics.sendPaypalErrorEvent("Paypal transaction error")
              _state.postValue(State.Error(R.string.unknown_error))
            }
          }
        }
        .subscribe({}, {
          Log.d(TAG, it.toString())
          topUpAnalytics.sendPaypalErrorEvent("Paypal transaction error")
          _state.postValue(State.Error(R.string.unknown_error))
        })
    )
  }

  fun createToken() {
    compositeDisposable.add(
      createPaypalTokenUseCase()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          Log.d(TAG, "Successful Token creation ")
          authenticatedToken = it.token
          _state.postValue(State.WebViewAuthentication(it.redirect.url))
        }
        .subscribe({}, {
          Log.d(TAG, it.toString())
          topUpAnalytics.sendPaypalErrorEvent("Paypal createToken error")
          _state.postValue(State.Error(R.string.unknown_error))
        })
    )
  }

  fun startBillingAgreement(
    amount: String, currency: String
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
              currency
            )
          }
          .subscribe({}, {
            Log.d(TAG, it.toString())
            topUpAnalytics.sendPaypalErrorEvent("Paypal BillingAgreement error")
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

  private fun waitForSuccess(hash: String?, uid: String?, amount: String) {
    compositeDisposable.add(
      waitForSuccessPaypalUseCase(uid ?: "")
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .subscribe(
          {
            when (it.status) {
              PaymentModel.Status.COMPLETED -> {
                Log.d(TAG, "Settled transaction polling completed")
                topUpAnalytics.sendPaypalSuccessEvent(amount)
                _state.postValue(State.SuccessPurchase(it.hash, it.uid))
              }
              PaymentModel.Status.FAILED, PaymentModel.Status.FRAUD, PaymentModel.Status.CANCELED,
              PaymentModel.Status.INVALID_TRANSACTION -> {
                Log.d(TAG, "Error on transaction on Settled transaction polling")
                topUpAnalytics.sendPaypalErrorEvent(
                  "Error on transaction on Settled transaction polling ${it.status.name}"
                )
                _state.postValue(State.Error(R.string.unknown_error))
              }
              else -> { /* pending */ }
            }
          },
          {
            Log.d(TAG, "Error on Settled transaction polling")
            topUpAnalytics.sendPaypalErrorEvent("Error on Settled transaction polling")
          })
    )
  }

  fun createBundle(
    priceAmount: String, priceCurrency: String,
    fiatCurrencySymbol: String, bonus: String
  ): Bundle {
    return billingMessagesMapper.topUpBundle(
      priceAmount, priceCurrency, bonus,
      fiatCurrencySymbol
    )
  }

  fun showSupport(gamificationLevel: Int) {
    compositeDisposable.add(
      supportInteractor.showSupport(gamificationLevel).subscribe({}, { it.printStackTrace() })
    )
  }

  companion object {
    private val TAG = PayPalTopupViewModel::class.java.simpleName
  }

}
