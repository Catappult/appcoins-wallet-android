package com.asfoundation.wallet.topup.amazonPay

import android.annotation.SuppressLint
import android.text.format.DateUtils
import androidx.lifecycle.ViewModel

import com.appcoins.wallet.core.network.microservices.model.AmazonPayCheckoutSessionRequest
import com.appcoins.wallet.core.network.microservices.model.AmazonPayTransaction
import com.appcoins.wallet.core.network.microservices.model.AmazonPrice
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.amazonPay.usecases.CreateAmazonPayTransactionTopUpUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.DeleteAmazonPayChargePermissionUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.GetAmazonPayChargePermissionLocalStorageUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.GetAmazonPayChargePermissionUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.GetAmazonPayCheckoutSessionIdUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.PatchAmazonPayCheckoutSessionUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.SaveAmazonPayChargePermissionLocalStorageUseCase
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetTransactionStatusUseCase
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.topup.TopUpPaymentData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
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
class AmazonPayTopUpViewModel @Inject constructor(
  private val getTransactionStatusUseCase: GetTransactionStatusUseCase,
  private val createAmazonPayTransactionTopUpUseCase: CreateAmazonPayTransactionTopUpUseCase,
  private val getAmazonPayCheckoutSessionIdUseCase: GetAmazonPayCheckoutSessionIdUseCase,
  private val getAmazonPayChargePermissionUseCase: GetAmazonPayChargePermissionUseCase,
  private val saveAmazonPayChargePermissionLocalStorageUseCase: SaveAmazonPayChargePermissionLocalStorageUseCase,
  private val getAmazonPayChargePermissionLocalStorageUseCase: GetAmazonPayChargePermissionLocalStorageUseCase,
  private val patchAmazonPayCheckoutSessionUseCase: PatchAmazonPayCheckoutSessionUseCase,
  private val deleteAmazonPayChargePermissionUseCase: DeleteAmazonPayChargePermissionUseCase,
  private val displayChatUseCase: DisplayChatUseCase,
  private val analytics: TopUpAnalytics
) : ViewModel() {

  lateinit var paymentData: TopUpPaymentData
  private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
  var uiState: StateFlow<UiState> = _uiState
  var amazonTransaction: AmazonPayTransaction? = null
  private val JOB_UPDATE_INTERVAL_MS = 5 * DateUtils.SECOND_IN_MILLIS
  private val JOB_TIMEOUT_MS = 120 * DateUtils.SECOND_IN_MILLIS
  private var jobTransactionStatus: Job? = null
  private val timerTransactionStatus = Timer()
  private var isTimerRunning = false
  val scope = CoroutineScope(Dispatchers.Main)
  var runningCustomTab = false

  @SuppressLint("CheckResult")
  fun getPaymentLink() {
    getAmazonPayChargePermissionUseCase()
      .flatMap { chargePermissionId ->
        saveAmazonPayChargePermissionLocalStorageUseCase(chargePermissionId = chargePermissionId.chargePermissionId)
        createAmazonPayTransaction(chargePermissionId.chargePermissionId)
      }
      .onErrorResumeNext {
        createAmazonPayTransaction(null)
      }
      .doOnSubscribe { _uiState.value = UiState.Loading }
      .doOnSuccess { amazonTransactionResult ->
        validateResultOfPaymentLink(amazonTransactionResult)
      }
      .subscribe({}, { _ -> _uiState.value = UiState.Error })
  }

  private fun createAmazonPayTransaction(chargePermissionId: String?): Single<AmazonPayTransaction> {

    val price = AmazonPrice(value = paymentData.fiatValue, currency = paymentData.fiatCurrencyCode)
    return createAmazonPayTransactionTopUpUseCase(
      price = price,
      chargePermissionId = chargePermissionId
    )
  }

  private fun validateResultOfPaymentLink(amazonPayTransaction: AmazonPayTransaction) {
    amazonTransaction = amazonPayTransaction
    when {
      amazonPayTransaction.errorCode == null && !amazonPayTransaction.redirectUrl.isNullOrEmpty() ->
        _uiState.value = UiState.PaymentRedirect3ds

      amazonPayTransaction.errorCode == null && getAmazonPayChargePermissionLocalStorageUseCase().isEmpty() ->
        _uiState.value = UiState.PaymentLinkSuccess

      amazonPayTransaction.errorCode == null && getAmazonPayChargePermissionLocalStorageUseCase().isNotEmpty() ->
        startTransactionStatusTimer()

      else ->
        _uiState.value = UiState.Error
    }
  }

  fun launchChat() {
    displayChatUseCase()
  }

  fun getAmazonCheckoutSessionId() {
    if (!isTimerRunning && runningCustomTab) {
      val amazonPayCheckoutRequest =
        AmazonPayCheckoutSessionRequest(getAmazonPayCheckoutSessionIdUseCase())
      if (amazonPayCheckoutRequest.checkoutSessionId.isEmpty()) {
        _uiState.value = UiState.Error
        return
      } else {
        patchAmazonPayCheckoutSessionUseCase(
          amazonTransaction?.uid,
          amazonPayCheckoutRequest
        ).subscribe()
        startTransactionStatusTimer()
      }
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

  private fun getTransactionStatus() {
    amazonTransaction?.uid?.let { amazonUid ->
      getTransactionStatusUseCase(
        uid = amazonUid
      ).map {
        when (it.status) {
          Transaction.Status.COMPLETED -> {
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
            stopTransactionStatusTimer()
            analytics.sendErrorEvent(
              paymentData.appcValue.toDouble(), "top_up", PaymentType.AMAZONPAY.name, "", ""
            )
            if (getAmazonPayChargePermissionLocalStorageUseCase().isNotEmpty()) {
              removeAmazonPayChargePermission()
            }
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

  @SuppressLint("CheckResult")
  fun removeAmazonPayChargePermission() {
    deleteAmazonPayChargePermissionUseCase.invoke()
      .subscribe(
        {
          saveAmazonPayChargePermissionLocalStorageUseCase("")
        },
        {}
      )
  }

}