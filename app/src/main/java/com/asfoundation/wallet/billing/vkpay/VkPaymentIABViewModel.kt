package com.asfoundation.wallet.billing.vkpay

import android.text.format.DateUtils
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.core.network.microservices.model.VkPayTransaction
import com.appcoins.wallet.core.network.microservices.model.VkPrice
import com.asf.wallet.R
import com.asfoundation.wallet.billing.vkpay.usecases.CreateVkPayTransactionTopUpUseCase
import com.asfoundation.wallet.billing.vkpay.usecases.CreateVkPayTransactionUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetTransactionStatusUseCase
import com.asfoundation.wallet.topup.TopUpPaymentData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject


sealed class VkPaymentIABSideEffect : SideEffect {
  object ShowLoading : VkPaymentIABSideEffect()
  data class ShowError(val message: Int?) : VkPaymentIABSideEffect()
  object ShowSuccess : VkPaymentIABSideEffect()
}

data class VkPaymentIABState(
  val vkTransaction: Async<VkPayTransaction> = Async.Uninitialized
) : ViewState

@HiltViewModel
class VkPaymentIABViewModel @Inject constructor(
  private val createVkPayTransactionUseCase: CreateVkPayTransactionUseCase,
  private val getTransactionStatusUseCase: GetTransactionStatusUseCase
) :
  BaseViewModel<VkPaymentIABState, VkPaymentIABSideEffect>(
    VkPaymentIABState()
  ) {

  private var transactionUid: String? = null
  private val JOB_UPDATE_INTERVAL_MS = 20 * DateUtils.SECOND_IN_MILLIS
  private val JOB_TIMEOUT_MS = 180 * DateUtils.SECOND_IN_MILLIS
  private var jobTransactionStatus: Job? = null
  private val timerTransactionStatus = Timer()
  private var isTimerRunning = false
  val scope = CoroutineScope(Dispatchers.Main)

  fun getPaymentLink(transactionBuilder: TransactionBuilder, amount: String, fiatCurrencySymbol: String) {
    val price = VkPrice(value = amount, currency = fiatCurrencySymbol)
    createVkPayTransactionUseCase(
      price = price, reference = transactionBuilder.orderReference, origin = transactionBuilder.origin, metadata = transactionBuilder.payload,
      sku = transactionBuilder.skuId, callbackUrl = transactionBuilder.callbackUrl, transactionType = transactionBuilder.type,
      developerWallet = transactionBuilder.toAddress(), referrerUrl = transactionBuilder.referrerUrl, packageName = transactionBuilder.domain
    ).asAsyncToState {
      copy(vkTransaction = it)
    }.scopedSubscribe()
  }

  fun startTransactionStatusTimer() {
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
        sendSideEffect { VkPaymentIABSideEffect.ShowError(R.string.unknown_error) }
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
            sendSideEffect { VkPaymentIABSideEffect.ShowSuccess }
          }

          Transaction.Status.INVALID_TRANSACTION,
          Transaction.Status.FAILED,
          Transaction.Status.CANCELED,
          Transaction.Status.FRAUD -> {
            stopTransactionStatusTimer()
            sendSideEffect {
              VkPaymentIABSideEffect.ShowError(
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