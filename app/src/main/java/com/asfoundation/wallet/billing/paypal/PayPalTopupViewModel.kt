package com.asfoundation.wallet.billing.paypal

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.network.microservices.model.PaypalTransaction
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.android_common.toSingleEvent
import com.asf.wallet.R
import com.asfoundation.wallet.billing.googlepay.GooglePayWebFragment
import com.asfoundation.wallet.billing.googlepay.models.CustomTabsPayResult
import com.asfoundation.wallet.billing.googlepay.models.GooglePayConst
import com.asfoundation.wallet.billing.paypal.usecases.CancelPaypalTokenUseCase
import com.asfoundation.wallet.billing.paypal.usecases.CreatePaypalAgreementUseCase
import com.asfoundation.wallet.billing.paypal.usecases.CreatePaypalTokenUseCase
import com.asfoundation.wallet.billing.paypal.usecases.CreatePaypalTransactionTopupUseCase
import com.asfoundation.wallet.billing.paypal.usecases.GetPayPalResultUseCase
import com.asfoundation.wallet.billing.paypal.usecases.WaitForSuccessPaypalUseCase
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.wallet.appcoins.feature.support.data.SupportInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
  private val getPayPalResultUseCase: GetPayPalResultUseCase,
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
  private var runningCustomTab = false
  private var hash: String? = null
  private var uid: String? = null


  fun startPayment(
    createTokenIfNeeded: Boolean = true,
    amount: String,
    currency: String,
  ) {
    topUpAnalytics.sendConfirmationEvent(
      amount.toDouble(),
      "top_up",
      PaymentMethodsAnalytics.PAYMENT_METHOD_PP_V2
    )
    attemptTransaction(
      createTokenIfNeeded = createTokenIfNeeded,
      amount = amount,
      currency = currency,
    )
  }

  fun processPayPalResult(amount: String, currency: String) {
    if (runningCustomTab) {
      runningCustomTab = false
      val result = getPayPalResultUseCase()
      when (result) {
        CustomTabsPayResult.SUCCESS.key -> {
          startBillingAgreement(
            amount = amount,
            currency = currency
          )
        }

        CustomTabsPayResult.ERROR.key -> {
          waitForSuccess(hash, uid, amount)
        }

        CustomTabsPayResult.CANCEL.key -> {
          waitForSuccess(hash, uid, amount)
        }

        else -> {
          waitForSuccess(hash, uid, amount)
        }
      }
    }
  }

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
              uid = it.uid
              hash = it.hash
              topUpAnalytics.sendPaypalSuccessEvent(amount)
              _state.postValue(State.SuccessPurchase(it.hash, it.uid))
            }

            PaypalTransaction.PaypalValidityState.NO_BILLING_AGREEMENT -> {
              Log.d(TAG, "No billing agreement. Create new token? $createTokenIfNeeded ")
              if (createTokenIfNeeded) {
                createToken()
              } else {
                Log.d(TAG, "No paypal billing agreement")
                topUpAnalytics.sendPaypalErrorEvent(
                  errorCode = it.errorCode,
                  errorDetails = it.errorMessage ?: ""
                )
                _state.postValue(State.Error(R.string.purchase_error_paypal))
              }
            }

            PaypalTransaction.PaypalValidityState.PENDING -> {
              hash = it.hash
              uid = it.uid
              waitForSuccess(it.hash, it.uid, amount)
            }

            PaypalTransaction.PaypalValidityState.ERROR -> {
              Log.d(TAG, "Paypal transaction error")
              topUpAnalytics.sendPaypalErrorEvent(errorDetails = "Paypal transaction error")
              _state.postValue(State.Error(R.string.purchase_error_paypal))
            }

            null -> {
              Log.d(TAG, "Paypal transaction error")
              topUpAnalytics.sendPaypalErrorEvent(errorDetails = "Paypal transaction error")
              _state.postValue(State.Error(R.string.purchase_error_paypal))
            }
          }
        }
        .subscribe({}, {
          Log.d(TAG, it.toString())
          topUpAnalytics.sendPaypalErrorEvent(errorDetails = "Paypal transaction error")
          _state.postValue(State.Error(R.string.purchase_error_paypal))
        })
    )
  }

  fun createToken() {
    compositeDisposable.add(
      createPaypalTokenUseCase()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          authenticatedToken = it.token
          _state.postValue(State.WebViewAuthentication(it.redirect.url))
        }
        .subscribe({}, {
          Log.d(TAG, it.toString())
          topUpAnalytics.sendPaypalErrorEvent(errorDetails = "Paypal createToken error")
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
            // after creating the billing agreement, don't create a new token if it fails
            attemptTransaction(
              createTokenIfNeeded = false,
              amount,
              currency
            )
          }
          .subscribe({}, {
            Log.d(TAG, it.toString())
            topUpAnalytics.sendPaypalErrorEvent(errorDetails = "Paypal BillingAgreement error")
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

  fun openUrlCustomTab(context: Context, url: String) {
    if (runningCustomTab) return
    runningCustomTab = true
    val customTabsBuilder = CustomTabsIntent.Builder().build()
    customTabsBuilder.intent.setPackage(GooglePayWebFragment.CHROME_PACKAGE_NAME)
    customTabsBuilder.launchUrl(context, Uri.parse(url))
  }

  private fun waitForSuccess(hash: String?, uid: String?, amount: String) {
    val disposableSuccessCheck =
      waitForSuccessPaypalUseCase(uid ?: "")
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .subscribe(
          {
            when (it.status) {
              PaymentModel.Status.COMPLETED -> {
                topUpAnalytics.sendPaypalSuccessEvent(amount)
                _state.postValue(State.SuccessPurchase(it.hash, it.uid))
              }

              PaymentModel.Status.FAILED, PaymentModel.Status.FRAUD, PaymentModel.Status.CANCELED,
              PaymentModel.Status.INVALID_TRANSACTION -> {
                Log.d(TAG, "Error on transaction on Settled transaction polling")
                topUpAnalytics.sendPaypalErrorEvent(
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
            topUpAnalytics.sendPaypalErrorEvent(errorDetails = "Error on Settled transaction polling")
          })
    // disposes the check after x seconds
    viewModelScope.launch {
      delay(GooglePayConst.GOOGLE_PAY_TIMEOUT)
      try {
        if (state.value !is State.SuccessPurchase) {
          Log.d(TAG, "Error on transaction on Settled transaction polling")
          topUpAnalytics.sendPaypalErrorEvent(
            errorDetails = "Error on transaction on Settled transaction polling ${state.value}"
          )
          _state.postValue(State.Error(R.string.unknown_error))
          disposableSuccessCheck.dispose()
        }
      } catch (_: Exception) {
      }
    }
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
