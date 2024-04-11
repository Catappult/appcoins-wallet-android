package com.asfoundation.wallet.onboarding_new_payment.mipay

import android.text.format.DateUtils
import androidx.activity.result.ActivityResult
import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.network.microservices.model.MiPayTransaction
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetMiPayLinkUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetTransactionStatusUseCase
import com.asfoundation.wallet.ui.iab.WebViewActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject


sealed class OnboardingMiPaySideEffect : SideEffect {
  data class NavigateToWebView(val uri: String) : OnboardingMiPaySideEffect()
  object NavigateBackToPaymentMethods : OnboardingMiPaySideEffect()
  object ShowLoading : OnboardingMiPaySideEffect()
  data class ShowError(val message: Int?) : OnboardingMiPaySideEffect()
  object ShowSuccess : OnboardingMiPaySideEffect()
}


data class OnboardingMiPayState(
  val transaction: Async<MiPayTransaction> = Async.Uninitialized
) :
  ViewState

@HiltViewModel
class OnboardingMiPayViewModel @Inject constructor(
  private val getPaymentLinkUseCase: GetMiPayLinkUseCase,
  private val events: OnboardingPaymentEvents,
  private val getTransactionStatusUseCase: GetTransactionStatusUseCase,
  savedStateHandle: SavedStateHandle
) :
  BaseViewModel<OnboardingMiPayState, OnboardingMiPaySideEffect>(
    OnboardingMiPayState()
  ) {

  private var transactionUid: String? = null
  private val JOB_UPDATE_INTERVAL_MS = 20 * DateUtils.SECOND_IN_MILLIS
  private val JOB_TIMEOUT_MS = 180 * DateUtils.SECOND_IN_MILLIS
  private var jobTransactionStatus: Job? = null
  private val timerTransactionStatus = Timer()
  private var isTimerRunning = false
  val scope = CoroutineScope(Dispatchers.Main)

  private var args: OnboardingMiPayFragmentArgs =
    OnboardingMiPayFragmentArgs.fromSavedStateHandle(savedStateHandle)

  init {
    getPaymentLink()
  }

  fun handleWebViewResult(result: ActivityResult) {
    when (result.resultCode) {
      WebViewActivity.FAIL,
      WebViewActivity.SUCCESS -> {
        startTransactionStatusTimer()
      }

      WebViewActivity.USER_CANCEL -> {
        events.sendAdyenPaymentConfirmationEvent(
          args.transactionBuilder,
          BillingAnalytics.ACTION_CANCEL,
          args.paymentType.name
        )
        sendSideEffect { OnboardingMiPaySideEffect.ShowError(R.string.unknown_error) }
      }
    }
  }

  private fun startTransactionStatusTimer() {
    // Set up a Timer to call getTransactionStatus() every 20 seconds
    if (!isTimerRunning) {
      timerTransactionStatus.schedule(object : TimerTask() {
        override fun run() {
          scope.launch {
            getTransactionStatus()
          }
        }
      }, 0L, JOB_UPDATE_INTERVAL_MS)
      isTimerRunning = true
      // Set up a CoroutineJob that will automatically cancel after 180 seconds
      jobTransactionStatus = scope.launch {
        delay(JOB_TIMEOUT_MS)
        sendSideEffect { OnboardingMiPaySideEffect.ShowError(R.string.unknown_error) }
        timerTransactionStatus.cancel()
      }
    }
  }

  fun stopTransactionStatusTimer() {
    jobTransactionStatus?.cancel()
    timerTransactionStatus.cancel()
    isTimerRunning = false
  }

  private fun getPaymentLink() {
    getPaymentLinkUseCase(
      data = args.transactionBuilder,
      currency = args.currency,
      packageName = args.transactionBuilder.domain,
      amount = args.amount,
      paymentType = args.paymentType.subTypes.first(),
    ).asAsyncToState {
      args.transactionBuilder.let { transactionBuilder ->
        events.sendLocalNavigationToUrlEvents(
          BuildConfig.APPLICATION_ID,
          transactionBuilder.skuId,
          transactionBuilder.amount().toString(),
          transactionBuilder.type,
          transactionBuilder.chainId.toString()
        )
      }
      transactionUid = it.value?.uid
      copy(transaction = it)
    }.scopedSubscribe()
  }

  private fun getTransactionStatus() {
    transactionUid?.let { uid ->
      getTransactionStatusUseCase(
        uid = uid
      ).map {
        when (it.status) {
          Transaction.Status.COMPLETED -> {
            stopTransactionStatusTimer()
            events.sendPaymentConclusionEvents(
              packageName = BuildConfig.APPLICATION_ID,
              skuId = args.transactionBuilder.skuId,
              amount = args.transactionBuilder.amount(),
              type = args.transactionBuilder.type,
              paymentId = args.transactionBuilder.chainId.toString(),
              txId = uid,
              amountUsd = args.transactionBuilder.amountUsd
            )
            sendSideEffect { OnboardingMiPaySideEffect.ShowSuccess }
          }

          Transaction.Status.INVALID_TRANSACTION,
          Transaction.Status.FAILED,
          Transaction.Status.CANCELED,
          Transaction.Status.FRAUD -> {
            stopTransactionStatusTimer()
            sendSideEffect {
              OnboardingMiPaySideEffect.ShowError(
                R.string.purchase_error_wallet_block_code_403
              )
            }
          }

          Transaction.Status.PENDING,
          Transaction.Status.PENDING_SERVICE_AUTHORIZATION,
          Transaction.Status.PROCESSING,
          Transaction.Status.PENDING_USER_PAYMENT,
          Transaction.Status.SETTLED -> {
          }
        }
      }.scopedSubscribe()
    }
  }

  fun handleBackButton() {
    sendSideEffect { OnboardingMiPaySideEffect.NavigateBackToPaymentMethods }
  }

}