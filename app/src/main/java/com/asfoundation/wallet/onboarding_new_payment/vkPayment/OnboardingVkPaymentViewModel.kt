package com.asfoundation.wallet.onboarding_new_payment.vkPayment

import android.text.format.DateUtils
import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.core.network.microservices.model.VkPayTransaction
import com.appcoins.wallet.core.network.microservices.model.VkPrice
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.vkpay.usecases.CreateVkPayTransactionUseCase
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetTransactionStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

sealed class OnboardingVkPaymentSideEffect : SideEffect {
  object ShowLoading : OnboardingVkPaymentSideEffect()
  object ShowError : OnboardingVkPaymentSideEffect()
  object ShowSuccess : OnboardingVkPaymentSideEffect()
  object PaymentLinkSuccess : OnboardingVkPaymentSideEffect()
  data class NavigateBackToGame(val appPackageName: String) : OnboardingVkPaymentSideEffect()
  object NavigateToExploreWallet : OnboardingVkPaymentSideEffect()
}

data class OnboardingVkPaymentStates(
  val vkTransaction: Async<VkPayTransaction> = Async.Uninitialized
) : ViewState

@HiltViewModel
class OnboardingVkPaymentViewModel @Inject constructor(
  private val createVkPayTransactionUseCase: CreateVkPayTransactionUseCase,
  private val getTransactionStatusUseCase: GetTransactionStatusUseCase,
  private val events: OnboardingPaymentEvents,
  private val getCurrentWalletUseCase: GetCurrentWalletUseCase,
  savedStateHandle: SavedStateHandle
) :
  BaseViewModel<OnboardingVkPaymentStates, OnboardingVkPaymentSideEffect>(
    OnboardingVkPaymentStates()
  ) {

  var transactionUid: String? = null
  var walletAddress: String = ""
  private val JOB_UPDATE_INTERVAL_MS = 5 * DateUtils.SECOND_IN_MILLIS
  private val JOB_TIMEOUT_MS = 600 * DateUtils.SECOND_IN_MILLIS
  private var jobTransactionStatus: Job? = null
  private val timerTransactionStatus = Timer()
  private var isTimerRunning = false
  val scope = CoroutineScope(Dispatchers.Main)
  var isFirstGetPaymentLink = true
  private var args: OnboardingVkPaymentFragmentArgs =
    OnboardingVkPaymentFragmentArgs.fromSavedStateHandle(savedStateHandle)

  fun getPaymentLink(email: String, phone: String) {
    isFirstGetPaymentLink = false
    val price = VkPrice(value = args.amount, currency = args.currency)
    getCurrentWalletUseCase().doOnSuccess {
      walletAddress = it.address
    }.scopedSubscribe()
    createVkPayTransactionUseCase(
      price = price,
      reference = args.transactionBuilder.orderReference,
      origin = args.transactionBuilder.origin,
      metadata = args.transactionBuilder.payload,
      sku = args.transactionBuilder.skuId,
      callbackUrl = args.transactionBuilder.callbackUrl,
      transactionType = args.transactionBuilder.type,
      referrerUrl =  if (args.transactionBuilder.type == "INAPP") null else args.transactionBuilder.referrerUrl,
      packageName = args.transactionBuilder.domain,
      email = email,
      phone = phone
    ).asAsyncToState {
      copy(vkTransaction = it)
    }.doOnSuccess {
      sendSideEffect { OnboardingVkPaymentSideEffect.PaymentLinkSuccess }
    }.scopedSubscribe()
  }

  fun startTransactionStatusTimer() {
    // Set up a Timer to call getTransactionStatus() every 5 seconds
    if (!isTimerRunning) {
      timerTransactionStatus.schedule(object : TimerTask() {
        override fun run() {
          scope.launch {
            getTransactionStatus()
          }
        }
      }, 0L, JOB_UPDATE_INTERVAL_MS)
      isTimerRunning = true
      // Set up a CoroutineJob that will automatically cancel after 600 seconds
      jobTransactionStatus = scope.launch {
        delay(JOB_TIMEOUT_MS)
        sendSideEffect { OnboardingVkPaymentSideEffect.ShowError }
        timerTransactionStatus.cancel()
      }
    }
  }

  private fun stopTransactionStatusTimer() {
    jobTransactionStatus?.cancel()
    timerTransactionStatus.cancel()
    isTimerRunning = false
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
            sendSideEffect { OnboardingVkPaymentSideEffect.ShowSuccess }
          }

          Transaction.Status.INVALID_TRANSACTION,
          Transaction.Status.FAILED,
          Transaction.Status.CANCELED,
          Transaction.Status.FRAUD -> {
            stopTransactionStatusTimer()
            events.sendAdyenPaymentConfirmationEvent(
              args.transactionBuilder,
              BillingAnalytics.ACTION_CANCEL,
              PaymentType.VKPAY.name
            )
            sendSideEffect {
              OnboardingVkPaymentSideEffect.ShowError
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

  fun handleBackToGameClick() {
    events.sendPaymentConclusionNavigationEvent(OnboardingPaymentEvents.BACK_TO_THE_GAME)
    sendSideEffect { OnboardingVkPaymentSideEffect.NavigateBackToGame(args.transactionBuilder.domain) }
  }

  fun handleExploreWalletClick() {
    events.sendPaymentConclusionNavigationEvent(OnboardingPaymentEvents.EXPLORE_WALLET)
    sendSideEffect { OnboardingVkPaymentSideEffect.NavigateToExploreWallet }
  }

}