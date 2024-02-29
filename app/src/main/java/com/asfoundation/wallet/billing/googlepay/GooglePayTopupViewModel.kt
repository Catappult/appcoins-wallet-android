package com.asfoundation.wallet.billing.googlepay

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.CANCELED
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.COMPLETED
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.FAILED
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.FRAUD
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.INVALID_TRANSACTION
import com.appcoins.wallet.core.network.microservices.model.GooglePayWebTransaction
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.android_common.toSingleEvent
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.googlepay.models.GooglePayConst
import com.asfoundation.wallet.billing.googlepay.models.GooglePayResult
import com.asfoundation.wallet.billing.googlepay.usecases.BuildGooglePayUrlUseCase
import com.asfoundation.wallet.billing.googlepay.usecases.CreateGooglePayTransactionTopupUseCase
import com.asfoundation.wallet.billing.googlepay.usecases.GetGooglePayResultUseCase
import com.asfoundation.wallet.billing.googlepay.usecases.GetGooglePayUrlUseCase
import com.asfoundation.wallet.billing.googlepay.usecases.WaitForSuccessUseCase
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.wallet.appcoins.feature.support.data.SupportInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GooglePayTopupViewModel @Inject constructor(
  private val getGooglePayUrlUseCase: GetGooglePayUrlUseCase,
  private val buildGooglePayUrlUseCase: BuildGooglePayUrlUseCase,
  private val createGooglePayTransactionTopupUseCase: CreateGooglePayTransactionTopupUseCase,
  private val waitForSuccessUseCase: WaitForSuccessUseCase,
  private val getGooglePayResultUseCase: GetGooglePayResultUseCase,
  private val billingMessagesMapper: BillingMessagesMapper,
  private val supportInteractor: SupportInteractor,
  private val topUpAnalytics: TopUpAnalytics,
  rxSchedulers: RxSchedulers
) : ViewModel() {

  sealed class State {
    object Start : State()
    data class Error(val stringRes: Int) : State()
    data class WebAuthentication(val url: String) : State()
    data class SuccessPurchase(val hash: String?, val uid: String?) : State()
    object GooglePayBack : State()
  }

  private val _state = MutableLiveData<State>(State.Start)
  val state = _state.toSingleEvent()

  private var compositeDisposable: CompositeDisposable = CompositeDisposable()

  private var authenticatedToken: String? = null

  val networkScheduler = rxSchedulers.io
  val viewScheduler = rxSchedulers.main

  var uid: String? = null
  var shouldStartPayment = true
  var runningCustomTab = false
  var isFirstRun: Boolean = true

  fun startPayment(
    amount: String,
    currency: String,
  ) {
    if (shouldStartPayment) {
      shouldStartPayment = false
      topUpAnalytics.sendConfirmationEvent(
        amount.toDouble(),
        "top_up",
        PaymentMethodsAnalytics.PAYMENT_METHOD_GOOGLEPAY_WEB
      )
      compositeDisposable.add(
        getGooglePayUrlUseCase()
          .subscribeOn(networkScheduler)
          .observeOn(viewScheduler)
          .flatMap { urls ->
            createTransaction(
              amount = amount,
              currency = currency,
              returnUrl = urls.returnUrl,
            ).map { transaction ->
              uid = transaction.uid
              val googlePayUrl = buildGooglePayUrlUseCase(
                url = urls.url,
                sessionId = transaction.sessionId ?: "",
                sessionData = transaction.sessionData ?: "",
                price = amount,
                currency = currency,
              )
              Log.d("url", googlePayUrl)
              _state.postValue(State.WebAuthentication(googlePayUrl))
            }
          }.subscribe({}, {
            Log.d(TAG, it.toString())
            topUpAnalytics.sendGooglePayErrorEvent(errorDetails = "GooglePay transaction error")
            _state.postValue(State.Error(R.string.purchase_error_google_pay))
          })
      )
    }
  }

  fun createTransaction(
    amount: String,
    currency: String,
    returnUrl: String,
  ): Single<GooglePayWebTransaction> {
    return createGooglePayTransactionTopupUseCase(
      value = amount,
      currency = currency,
      method = PaymentType.GOOGLEPAY_WEB.subTypes[0],
      returnUrl = returnUrl,
    )
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .doOnSuccess {
        when (it?.validity) {
          GooglePayWebTransaction.GooglePayWebValidityState.COMPLETED -> {
            topUpAnalytics.sendGooglePaySuccessEvent(amount)
            _state.postValue(State.SuccessPurchase(it.hash, it.uid))
          }

          GooglePayWebTransaction.GooglePayWebValidityState.PENDING -> {
          }

          GooglePayWebTransaction.GooglePayWebValidityState.ERROR -> {
            Log.d(TAG, "GooglePay transaction error")
            topUpAnalytics.sendGooglePayErrorEvent(errorDetails = "GooglePay transaction error")
            _state.postValue(State.Error(R.string.purchase_error_google_pay))
          }

          null -> {
            Log.d(TAG, "GooglePay transaction error")
            topUpAnalytics.sendGooglePayErrorEvent(errorDetails = "GooglePay transaction error")
            _state.postValue(State.Error(R.string.purchase_error_google_pay))
          }
        }
      }
  }

  private fun waitForSuccess(
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
              topUpAnalytics.sendGooglePaySuccessEvent(amount)
              _state.postValue(State.SuccessPurchase(it.hash, it.uid))
            }

            FAILED, FRAUD, CANCELED, INVALID_TRANSACTION -> {
              Log.d(TAG, "Error on transaction on Settled transaction polling")
              topUpAnalytics.sendGooglePayErrorEvent(
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
          topUpAnalytics.sendGooglePayErrorEvent(errorDetails = "Error on Settled transaction polling")
        })
    // disposes the check after x seconds
    viewModelScope.launch {
      delay(GooglePayConst.GOOGLE_PAY_TIMEOUT)
      try {
        if (state.value !is State.SuccessPurchase && wasNonSuccess)
          _state.postValue(State.Error(R.string.purchase_error_google_pay))
        disposableSuccessCheck.dispose()
      } catch (_: Exception) {
      }
    }
  }

  fun processGooglePayResult(amount: String) {
    if (isFirstRun) {
      isFirstRun = false
    } else {
      if (runningCustomTab) {
        runningCustomTab = false
        val result = getGooglePayResultUseCase()
        when (result) {
          GooglePayResult.SUCCESS.key -> {
            waitForSuccess(uid, amount)
          }

          GooglePayResult.ERROR.key -> {
            topUpAnalytics.sendGooglePayErrorEvent("", "Error received from Web.")
            _state.postValue(State.Error(R.string.purchase_error_google_pay))
          }

          GooglePayResult.CANCEL.key -> {
            waitForSuccess(uid, amount, true)
          }

          else -> {
            waitForSuccess(uid, amount, true)
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
    private val TAG = GooglePayTopupViewModel::class.java.simpleName
  }

}
