package com.asfoundation.wallet.topup.amazonPay

import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.network.microservices.model.AmazonPayTransaction
import com.appcoins.wallet.core.network.microservices.model.AmazonPrice
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.core.network.microservices.model.VkPayTransaction
import com.appcoins.wallet.core.network.microservices.model.VkPrice
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatusCompound
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.amazonPay.usecases.CreateAmazonPayTransactionTopUpUseCase
import com.asfoundation.wallet.billing.vkpay.usecases.CreateVkPayTransactionTopUpUseCase
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.manage_wallets.ManageWalletViewModel
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetTransactionStatusUseCase
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.topup.TopUpPaymentData
import com.asfoundation.wallet.topup.vkPayment.VkPaymentTopUpSideEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

sealed class UiState {
  object Idle : UiState()
  object Loading : UiState()
  data class Error(val message: String?) : UiState()
  object PaymentLinkSuccess : UiState()
  object Success : UiState()
}

@HiltViewModel
class AmazonPayTopUpViewModel @Inject constructor(
  private val getTransactionStatusUseCase: GetTransactionStatusUseCase,
  private val createAmazonPayTransactionTopUpUseCase: CreateAmazonPayTransactionTopUpUseCase,
  private val displayChatUseCase: DisplayChatUseCase,
  private val analytics: TopUpAnalytics
) : ViewModel() {

  lateinit var paymentData: TopUpPaymentData
  private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
  var uiState: StateFlow<UiState> = _uiState
  var amazonTransaction: AmazonPayTransaction? = null
  private val JOB_UPDATE_INTERVAL_MS = 5 * DateUtils.SECOND_IN_MILLIS
  private val JOB_TIMEOUT_MS = 600 * DateUtils.SECOND_IN_MILLIS
  private var jobTransactionStatus: Job? = null
  private val timerTransactionStatus = Timer()
  private var isTimerRunning = false
  val scope = CoroutineScope(Dispatchers.Main)
  var runningCustomTab = false

  fun getPaymentLink() {
    val price = AmazonPrice(value = paymentData.fiatValue, currency = paymentData.fiatCurrencyCode)
    createAmazonPayTransactionTopUpUseCase(
      price = price,
    ).doOnSubscribe { _uiState.value = UiState.Loading }
      .doOnSuccess { amazonTransactionResult ->
        if (amazonTransactionResult.errorCode == null) {
          _uiState.value = UiState.PaymentLinkSuccess
          amazonTransaction = amazonTransactionResult
        } else {
          _uiState.value = UiState.Error(amazonTransactionResult.errorCode)
          amazonTransaction = amazonTransactionResult
        }
      }
      .subscribe()
  }


  fun launchChat() {
    displayChatUseCase()
  }

  fun startTransactionStatusTimer() {
    Log.d("amazonpaytransaction", "runningCustomTab "  + runningCustomTab)
    Log.d("amazonpaytransaction", "runningCustomTab "  + (!isTimerRunning && runningCustomTab))
    // Set up a Timer to call getTransactionStatus() every 20 seconds
    if (!isTimerRunning && runningCustomTab) {
      timerTransactionStatus.schedule(object : TimerTask() {
        override fun run() {
          scope.launch {
            Log.d("amazonpaytransaction", "startTransactionStatusTimer: getTransactionStatus")
            getTransactionStatus()
          }
        }
      }, 0L, JOB_UPDATE_INTERVAL_MS)
      Log.d("amazonpaytransaction", "startTransactionStatusTimer: runningCustomTab")
      // Set up a CoroutineJob that will automatically cancel after 180 seconds
      jobTransactionStatus = scope.launch {
        try {
          delay(JOB_TIMEOUT_MS)
          Log.d("amazonpaytransaction", "startTransactionStatusTimer: UiState.Error - Timeout occurred")
          _uiState.value = UiState.Error(R.string.unknown_error.toString())
        } finally {
          Log.d("amazonpaytransaction", "startTransactionStatusTimer: Cancelling timer and job")
          timerTransactionStatus.cancel()
          isTimerRunning = false
        }
      }
    }
    runningCustomTab = false
    isTimerRunning = true
  }

  private fun stopTransactionStatusTimer() {
    jobTransactionStatus?.cancel()
    timerTransactionStatus.cancel()
    isTimerRunning = false
  }

  private fun getTransactionStatus() {
    amazonTransaction?.uid?.let {
      getTransactionStatusUseCase(
        uid = it
      ).map {
        when (it.status) {
          Transaction.Status.COMPLETED -> {
            Log.d("amazonpaytransaction", "startTransactionStatusTimer: UiState.COMPLETED")
            stopTransactionStatusTimer()
            analytics.sendConfirmationEvent(
              paymentData.appcValue.toDouble(), "top_up", PaymentType.AMAZONPAY.name
            )
            _uiState.value = UiState.Success
          }

          Transaction.Status.INVALID_TRANSACTION,
          Transaction.Status.FAILED,
          Transaction.Status.CANCELED,
          Transaction.Status.FRAUD -> {
            Log.d("amazonpaytransaction", "startTransactionStatusTimer: UiState.FRAUD")
            stopTransactionStatusTimer()
            analytics.sendErrorEvent(
              paymentData.appcValue.toDouble(), "top_up", PaymentType.AMAZONPAY.name, "", ""
            )
            _uiState.value = UiState.Error(R.string.purchase_error_fraud_code_20.toString())
          }

          Transaction.Status.PENDING,
          Transaction.Status.PENDING_SERVICE_AUTHORIZATION,
          Transaction.Status.PROCESSING,
          Transaction.Status.PENDING_USER_PAYMENT,
          Transaction.Status.SETTLED -> {
            Log.d("amazonpaytransaction", "startTransactionStatusTimer: Pendindg")
          }
        }
      }.subscribe()
    }

  }


}