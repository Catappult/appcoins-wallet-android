package com.asfoundation.wallet.onboarding_new_payment.amazonPay

import android.annotation.SuppressLint
import android.text.format.DateUtils
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

import com.appcoins.wallet.core.network.microservices.model.AmazonPayCheckoutSessionRequest
import com.appcoins.wallet.core.network.microservices.model.AmazonPayTransaction
import com.appcoins.wallet.core.network.microservices.model.AmazonPrice
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.billing.amazonPay.usecases.CreateAmazonPayTransactionUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.GetAmazonPayCheckoutSessionIdUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.PatchAmazonPayCheckoutSessionUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.onboarding_new_payment.OnboardingPaymentEvents
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetTransactionStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

sealed class UiState {
  object Idle : UiState()
  object Loading : UiState()
  object Error : UiState()
  object PaymentLinkSuccess : UiState()
  object PaymentRedirect3ds : UiState()
  object Success : UiState()
}

@HiltViewModel
class OnboardingAmazonPayViewModel @Inject constructor(
  private val getTransactionStatusUseCase: GetTransactionStatusUseCase,
  private val createAmazonPayTransactionUseCase: CreateAmazonPayTransactionUseCase,
  private val getAmazonPayCheckoutSessionIdUseCase: GetAmazonPayCheckoutSessionIdUseCase,
  private val patchAmazonPayCheckoutSessionUseCase: PatchAmazonPayCheckoutSessionUseCase,
  private val displayChatUseCase: DisplayChatUseCase,
  private val analytics: OnboardingPaymentEvents,
  private val rxSchedulers: RxSchedulers,
  savedStateHandle: SavedStateHandle,
) : ViewModel() {

  private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
  var uiState: StateFlow<UiState> = _uiState
  var amazonTransaction: AmazonPayTransaction? = null
  private val JOB_UPDATE_INTERVAL_MS = 5 * DateUtils.SECOND_IN_MILLIS
  private val JOB_TIMEOUT_MS = 120 * DateUtils.SECOND_IN_MILLIS
  private var jobTransactionStatus: Job? = null
  private val timerTransactionStatus = Timer()
  private var isTimerRunning = false
  val scope = CoroutineScope(Dispatchers.Main)
  private var args: OnboardingAmazonPayFragmentArgs =
    OnboardingAmazonPayFragmentArgs.fromSavedStateHandle(savedStateHandle)
  var runningCustomTab = false

  @SuppressLint("CheckResult")
  fun getPaymentLink() {
    val price = AmazonPrice(value = args.amount, currency = args.currency)
    createAmazonPayTransactionUseCase(
      price = price,
      reference = args.transactionBuilder.orderReference,
      origin = args.transactionBuilder.origin,
      metadata = args.transactionBuilder.payload,
      sku = args.transactionBuilder.skuId,
      callbackUrl = args.transactionBuilder.callbackUrl,
      transactionType = args.transactionBuilder.type,
      referrerUrl = args.transactionBuilder.referrerUrl,
      packageName = args.transactionBuilder.domain,
      chargePermissionId = null
    )
      .doOnSubscribe { _uiState.value = UiState.Loading }
      .doOnSuccess { amazonTransactionResult ->
        validateResultOfPaymentLink(amazonTransactionResult)
      }
      .subscribe({}, { _ -> _uiState.value = UiState.Error })
  }


  fun sendPaymentStartEvent(transactionBuilder: TransactionBuilder) {
    analytics.sendPurchaseStartEvent(transactionBuilder = transactionBuilder, oemId = args.transactionBuilder.oemIdSdk)

  }

  private fun validateResultOfPaymentLink(amazonPayTransaction: AmazonPayTransaction) {
    amazonTransaction = amazonPayTransaction
    when {
      amazonPayTransaction.errorCode == null && !amazonPayTransaction.redirectUrl.isNullOrEmpty() ->
        _uiState.value = UiState.PaymentRedirect3ds

      amazonPayTransaction.errorCode == null ->
        _uiState.value = UiState.PaymentLinkSuccess

      else ->
        _uiState.value = UiState.Error
    }
  }

  fun launchChat() {
    displayChatUseCase()
  }

  fun getAmazonCheckoutSessionId(sessionId: String? = null) {
    if (!isTimerRunning && runningCustomTab) {
      var amazonPayCheckoutRequest = AmazonPayCheckoutSessionRequest(
        sessionId ?: getAmazonPayCheckoutSessionIdUseCase()
      )
      if (amazonPayCheckoutRequest.checkoutSessionId.isEmpty()) {
        amazonPayCheckoutRequest = AmazonPayCheckoutSessionRequest(
          amazonTransaction?.checkoutSessionId ?: ""
        )
        if(amazonPayCheckoutRequest.checkoutSessionId.isEmpty()) {
          _uiState.value = UiState.Error
          return
        }
      }
      patchAmazonPayCheckoutSessionUseCase(
        amazonTransaction?.uid,
        amazonPayCheckoutRequest
      ).subscribe()
      startTransactionStatusTimer()
      runningCustomTab = false
      isTimerRunning = true
    }
  }

  private fun startTransactionStatusTimer() {
    jobTransactionStatus = scope.launch {
      try {
        withTimeout(JOB_TIMEOUT_MS) {
          while (isActive) {
            getTransactionStatus()
            delay(JOB_UPDATE_INTERVAL_MS)
          }
        }
      } catch (e: TimeoutCancellationException) {
        _uiState.value = UiState.Error
      } finally {
        isTimerRunning = false
      }
    }
  }

  private fun stopTransactionStatusTimer() {
    jobTransactionStatus?.cancel()
    timerTransactionStatus.cancel()
    isTimerRunning = false
  }

  fun sendPaymentSuccessEvent(transactionBuilder: TransactionBuilder, txId: String) {
    CompositeDisposable().add(
      Single.just(transactionBuilder)
        .observeOn(rxSchedulers.io)
        .doOnSuccess { transaction ->
          analytics.sendPaymentConclusionEvents(
            packageName = BuildConfig.APPLICATION_ID,
            skuId = transaction.skuId,
            amount = transaction.amount(),
            type = transaction.type,
            paymentId = transaction.chainId.toString(),
            txId = txId,
            amountUsd = transaction.amountUsd
          )
        }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  fun sendPaymentErrorEvent(
    errorCode: String? = null,
    errorMessage: String?,
    transactionBuilder: TransactionBuilder
  ) {
    CompositeDisposable().add(
      Single.just(transactionBuilder)
        .observeOn(rxSchedulers.io)
        .doOnSuccess { transaction ->
          analytics.sendPaymentErrorEvent(
            transactionBuilder = transactionBuilder,
            paymentType = args.paymentType,
            refusalCode =  errorCode?.toInt(),
            refusalReason = errorMessage ?: "",
            riskRules = transaction.type,
          )
        }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun getTransactionStatus() {
    amazonTransaction?.uid?.let { amazonUid ->
      getTransactionStatusUseCase(
        uid = amazonUid
      ).map {
        when (it.status) {
          Transaction.Status.COMPLETED -> {
            stopTransactionStatusTimer()
            _uiState.value = UiState.Success
          }

          Transaction.Status.INVALID_TRANSACTION,
          Transaction.Status.FAILED,
          Transaction.Status.CANCELED,
          Transaction.Status.FRAUD -> {
            stopTransactionStatusTimer()
            _uiState.value = UiState.Error
          }

          Transaction.Status.PENDING,
          Transaction.Status.PENDING_SERVICE_AUTHORIZATION,
          Transaction.Status.PROCESSING,
          Transaction.Status.PENDING_USER_PAYMENT,
          Transaction.Status.SETTLED -> {
          }
        }
      }.subscribe()
    }

  }

}