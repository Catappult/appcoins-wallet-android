package com.asfoundation.wallet.onboarding_new_payment.local_payments

import android.text.format.DateUtils
import androidx.activity.result.ActivityResult

import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.appcoins.wallet.ui.arch.data.Async
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetPaymentLinkUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetTransactionStatusUseCase
import com.asfoundation.wallet.ui.iab.WebViewActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject


sealed class OnboardingLocalPaymentSideEffect : SideEffect {
    data class NavigateToWebView(val uri: String) : OnboardingLocalPaymentSideEffect()
    object NavigateBackToPaymentMethods : OnboardingLocalPaymentSideEffect()
    object ShowLoading : OnboardingLocalPaymentSideEffect()
    data class ShowError(val message: Int?) : OnboardingLocalPaymentSideEffect()
    object ShowSuccess : OnboardingLocalPaymentSideEffect()
}


data class OnboardingLocalPaymentState(
    val transaction: Async<Transaction> = Async.Uninitialized
) :
    ViewState

@HiltViewModel
class OnboardingLocalPaymentViewModel @Inject constructor(
    private val getPaymentLinkUseCase: GetPaymentLinkUseCase,
    private val events: OnboardingPaymentEvents,
    private val getTransactionStatusUseCase: GetTransactionStatusUseCase,
    savedStateHandle: SavedStateHandle
) :
    BaseViewModel<OnboardingLocalPaymentState, OnboardingLocalPaymentSideEffect>(
        OnboardingLocalPaymentState()
    ) {

    private var transactionUid: String? = null
    private val JOB_UPDATE_INTERVAL_MS = 20 * DateUtils.SECOND_IN_MILLIS
    private val JOB_TIMEOUT_MS = 180 * DateUtils.SECOND_IN_MILLIS
    private var jobTransactionStatus: Job? = null
    private val timerTransactionStatus = Timer()
    private var isTimerRunning = false
    val scope = CoroutineScope(Dispatchers.Main)

    private var args: OnboardingLocalPaymentFragmentArgs =
        OnboardingLocalPaymentFragmentArgs.fromSavedStateHandle(savedStateHandle)

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
                events.sendPayPalConfirmationEvent(args.transactionBuilder, "cancel")
                sendSideEffect { OnboardingLocalPaymentSideEffect.ShowError(R.string.unknown_error) }
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
                sendSideEffect { OnboardingLocalPaymentSideEffect.ShowError(R.string.unknown_error) }
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
            paymentType = args.paymentType
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
                            BuildConfig.APPLICATION_ID,
                            args.transactionBuilder.skuId,
                            args.transactionBuilder.amount(),
                            args.transactionBuilder.type,
                            args.transactionBuilder.chainId.toString()
                        )
                        sendSideEffect { OnboardingLocalPaymentSideEffect.ShowSuccess }
                    }
                    Transaction.Status.INVALID_TRANSACTION,
                    Transaction.Status.FAILED,
                    Transaction.Status.CANCELED,
                    Transaction.Status.FRAUD -> {
                        stopTransactionStatusTimer()
                        sendSideEffect {
                            OnboardingLocalPaymentSideEffect.ShowError(
                                R.string.purchase_error_wallet_block_code_403
                            )
                        }
                    }
                    Transaction.Status.PENDING,
                    Transaction.Status.PENDING_SERVICE_AUTHORIZATION,
                    Transaction.Status.PROCESSING,
                    Transaction.Status.PENDING_USER_PAYMENT,
                    Transaction.Status.SETTLED -> {}
                }
            }.scopedSubscribe()
        }
    }

    fun handleBackButton() {
        sendSideEffect { OnboardingLocalPaymentSideEffect.NavigateBackToPaymentMethods }
    }

}