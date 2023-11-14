package com.asfoundation.wallet.billing.vkpay

import android.text.format.DateUtils
import androidx.compose.runtime.mutableStateOf
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.core.network.microservices.model.VkPayTransaction
import com.appcoins.wallet.core.network.microservices.model.VkPrice
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.asf.wallet.R
import com.asfoundation.wallet.billing.vkpay.usecases.CreateVkPayTransactionUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetTransactionStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
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
  object PaymentLinkSuccess : VkPaymentIABSideEffect()
}

data class VkPaymentIABState(
  val vkTransaction: Async<VkPayTransaction> = Async.Uninitialized
) : ViewState

@HiltViewModel
class VkPaymentIABViewModel @Inject constructor(
  private val createVkPayTransactionUseCase: CreateVkPayTransactionUseCase,
  private val getTransactionStatusUseCase: GetTransactionStatusUseCase,
  private val getCurrentWalletUseCase: GetCurrentWalletUseCase,
  private val rxSchedulers: RxSchedulers,
  private val analytics: BillingAnalytics,
) :
  BaseViewModel<VkPaymentIABState, VkPaymentIABSideEffect>(
    VkPaymentIABState()
  ) {

  var transactionUid: String? = null
  var walletAddress: String = ""
  private val JOB_UPDATE_INTERVAL_MS = 15 * DateUtils.SECOND_IN_MILLIS
  private val JOB_TIMEOUT_MS = 60 * DateUtils.SECOND_IN_MILLIS
  private var jobTransactionStatus: Job? = null
  private val timerTransactionStatus = Timer()
  private var isTimerRunning = false
  val scope = CoroutineScope(Dispatchers.Main)
  var hasVkUserAuthenticated: Boolean = false
  var hasVkPayAlreadyOpened: Boolean = false
  var isFirstGetPaymentLink = true
  private var compositeDisposable: CompositeDisposable = CompositeDisposable()
  val transactionVkData = mutableStateOf<VkPayTransaction?>(null)

  fun getPaymentLink(
    transactionBuilder: TransactionBuilder,
    amount: String,
    fiatCurrencySymbol: String,
    origin: String
  ) {
    isFirstGetPaymentLink = false
    val price = VkPrice(value = amount, currency = fiatCurrencySymbol)
    getCurrentWalletUseCase().doOnSuccess {
      walletAddress = it.address
    }.scopedSubscribe()
    createVkPayTransactionUseCase(
      price = price,
      reference = transactionBuilder.orderReference,
      origin = origin,
      metadata = transactionBuilder.payload,
      sku = transactionBuilder.skuId,
      callbackUrl = transactionBuilder.callbackUrl,
      transactionType = transactionBuilder.type,
      developerWallet = transactionBuilder.toAddress(),
      referrerUrl = transactionBuilder.referrerUrl,
      packageName = transactionBuilder.domain
    ).asAsyncToState {
      copy(vkTransaction = it)
    }.doOnSuccess {
      transactionVkData.value = it
      sendSideEffect { VkPaymentIABSideEffect.PaymentLinkSuccess }
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

  fun sendPaymentSuccessEvent(transactionBuilder: TransactionBuilder, txId: String) {
    compositeDisposable.add(
      Single.just(transactionBuilder)
      .observeOn(rxSchedulers.io)
      .doOnSuccess { transaction ->
        analytics.sendPaymentSuccessEvent(
          packageName = transactionBuilder.domain,
          skuDetails = transaction.skuId,
          value = transaction.amount().toString(),
          purchaseDetails = BillingAnalytics.PAYMENT_METHOD_VK_PAY,
          transactionType = transaction.type,
          txId = txId,
          valueUsd = transaction.amountUsd.toString()
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  fun sendPaymentStartEvent(transactionBuilder: TransactionBuilder?) {
    analytics.sendPaymentConfirmationEvent(
      transactionBuilder?.domain, transactionBuilder?.skuId,
      transactionBuilder?.amount()
        .toString(), BillingAnalytics.PAYMENT_METHOD_VK_PAY,
      transactionBuilder?.type, "BUY"
    )
  }

  fun sendPaymentErrorEvent(
    errorCode: String? = null,
    errorMessage: String?,
    transactionBuilder: TransactionBuilder
  ) {
    compositeDisposable.add(
      Single.just(transactionBuilder)
      .observeOn(rxSchedulers.io)
      .doOnSuccess { transaction ->
        analytics.sendPaymentErrorWithDetailsAndRiskEvent(
          transaction.domain,
          transaction.skuId,
          transaction.amount().toString(),
          BillingAnalytics.PAYMENT_METHOD_VK_PAY,
          transaction.type,
          errorCode ?: "",
          errorMessage ?: "",
          ""
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
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