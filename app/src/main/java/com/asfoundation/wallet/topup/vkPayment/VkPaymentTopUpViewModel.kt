package com.asfoundation.wallet.topup.vkPayment

import android.text.format.DateUtils
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.core.network.microservices.model.VkPayTransaction
import com.appcoins.wallet.core.network.microservices.model.VkPrice
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.appcoins.wallet.ui.widgets.CardPromotionItem
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.vkpay.usecases.CreateVkPayTransactionTopUpUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetTransactionStatusUseCase
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.topup.TopUpPaymentData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject


sealed class VkPaymentTopUpSideEffect : SideEffect {
  object ShowLoading : VkPaymentTopUpSideEffect()
  data class ShowError(val message: Int?) : VkPaymentTopUpSideEffect()
  object ShowSuccess : VkPaymentTopUpSideEffect()
  object PaymentLinkSuccess : VkPaymentTopUpSideEffect()
}

data class VkPaymentTopUpState(
  val vkTransaction: Async<VkPayTransaction> = Async.Uninitialized
) : ViewState

@HiltViewModel
class VkPaymentTopUpViewModel @Inject constructor(
  private val createVkPayTransactionTopUpUseCase: CreateVkPayTransactionTopUpUseCase,
  private val getTransactionStatusUseCase: GetTransactionStatusUseCase,
  private val getCurrentWalletUseCase: GetCurrentWalletUseCase,
  private val analytics: TopUpAnalytics
) :
  BaseViewModel<VkPaymentTopUpState, VkPaymentTopUpSideEffect>(
    VkPaymentTopUpState()
  ) {

  var transactionUid: String? = null
  var walletAddress: String = ""
  lateinit var paymentData: TopUpPaymentData
  private val JOB_UPDATE_INTERVAL_MS = 15 * DateUtils.SECOND_IN_MILLIS
  private val JOB_TIMEOUT_MS = 60 * DateUtils.SECOND_IN_MILLIS
  private var jobTransactionStatus: Job? = null
  private val timerTransactionStatus = Timer()
  private var isTimerRunning = false
  var isFirstGetPaymentLink = true
  val scope = CoroutineScope(Dispatchers.Main)

  fun getPaymentLink() {
    isFirstGetPaymentLink = false
    getCurrentWalletUseCase().doOnSuccess {
      walletAddress = it.address
    }.scopedSubscribe()
    val price = VkPrice(value = paymentData.fiatValue, currency = paymentData.fiatCurrencySymbol)
    createVkPayTransactionTopUpUseCase(
      price = price
    ).asAsyncToState {
      copy(vkTransaction = it)
    }.doOnSuccess {
      sendSideEffect { VkPaymentTopUpSideEffect.PaymentLinkSuccess }
    }.scopedSubscribe()
  }

  fun startTransactionStatusTimer() {
    Log.d("STATEEEE", "state vkTransaction:  startTransactionStatusTimer?")
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
        sendSideEffect { VkPaymentTopUpSideEffect.ShowError(R.string.unknown_error) }
        timerTransactionStatus.cancel()
      }
    }
  }

  private fun stopTransactionStatusTimer() {
    jobTransactionStatus?.cancel()
    timerTransactionStatus.cancel()
    isTimerRunning = false
  }

  fun sendConfirmationEvent(amout: Double, paymentId: String) {

  }

  private fun getTransactionStatus() {
    transactionUid?.let { uid ->
      getTransactionStatusUseCase(
        uid = uid
      ).map {
        when (it.status) {
          Transaction.Status.COMPLETED -> {
            stopTransactionStatusTimer()
            analytics.sendConfirmationEvent(
              paymentData.appcValue.toDouble(), "top_up", PaymentType.VKPAY.name
            )
            sendSideEffect { VkPaymentTopUpSideEffect.ShowSuccess }
          }

          Transaction.Status.INVALID_TRANSACTION,
          Transaction.Status.FAILED,
          Transaction.Status.CANCELED,
          Transaction.Status.FRAUD -> {
            stopTransactionStatusTimer()
            analytics.sendErrorEvent(
              paymentData.appcValue.toDouble(), "top_up", PaymentType.VKPAY.name, "", ""
            )
            sendSideEffect {
              VkPaymentTopUpSideEffect.ShowError(
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
}