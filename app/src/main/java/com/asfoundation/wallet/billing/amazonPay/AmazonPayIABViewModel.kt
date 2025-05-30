package com.asfoundation.wallet.billing.amazonPay

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics

import com.appcoins.wallet.core.network.microservices.model.AmazonPayCheckoutSessionRequest
import com.appcoins.wallet.core.network.microservices.model.AmazonPayTransaction
import com.appcoins.wallet.core.network.microservices.model.AmazonPrice
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.billing.amazonPay.usecases.CreateAmazonPayTransactionUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.GetAmazonPayChargePermissionLocalStorageUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.GetAmazonPayChargePermissionUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.GetAmazonPayCheckoutSessionIdUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.PatchAmazonPayCheckoutSessionUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.SaveAmazonPayChargePermissionLocalStorageUseCase
import com.asfoundation.wallet.billing.paypal.usecases.CreateSuccessBundleUseCase
import com.asfoundation.wallet.billing.vkpay.VkPaymentIABViewModel.SuccessInfo
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetTransactionStatusUseCase
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
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
  data class SendSuccessBundle(val bundle: Bundle) : UiState()
}

@HiltViewModel
class AmazonPayIABViewModel @Inject constructor(
  private val getTransactionStatusUseCase: GetTransactionStatusUseCase,
  private val createAmazonPayTransactionUseCase: CreateAmazonPayTransactionUseCase,
  private val getAmazonPayCheckoutSessionIdUseCase: GetAmazonPayCheckoutSessionIdUseCase,
  private val getAmazonPayChargePermissionUseCase: GetAmazonPayChargePermissionUseCase,
  private val saveAmazonPayChargePermissionLocalStorageUseCase: SaveAmazonPayChargePermissionLocalStorageUseCase,
  private val getAmazonPayChargePermissionLocalStorageUseCase: GetAmazonPayChargePermissionLocalStorageUseCase,
  private val patchAmazonPayCheckoutSessionUseCase: PatchAmazonPayCheckoutSessionUseCase,
  private val displayChatUseCase: DisplayChatUseCase,
  private val analytics: BillingAnalytics,
  private val inAppPurchaseInteractor: InAppPurchaseInteractor,
  private val createSuccessBundleUseCase: CreateSuccessBundleUseCase,
  private val rxSchedulers: RxSchedulers,
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
  var runningCustomTab = false
  var successInfo: SuccessInfo? = null

  @SuppressLint("CheckResult")
  fun getPaymentLink(
    transactionBuilder: TransactionBuilder,
    amount: String,
    fiatCurrencySymbol: String,
    origin: String?
  ) {
    getAmazonPayChargePermissionUseCase()
      .flatMap { chargePermissionId ->
        saveAmazonPayChargePermissionLocalStorageUseCase(chargePermissionId = chargePermissionId.chargePermissionId)
        createAmazonPayTransaction(
          chargePermissionId = chargePermissionId.chargePermissionId,
          transactionBuilder = transactionBuilder,
          amount = amount,
          fiatCurrencySymbol = fiatCurrencySymbol,
          origin = origin
        )
      }
      .onErrorResumeNext {
        createAmazonPayTransaction(
          chargePermissionId = null,
          transactionBuilder = transactionBuilder,
          amount = amount,
          fiatCurrencySymbol = fiatCurrencySymbol,
          origin = origin
        )
      }
      .doOnSubscribe { _uiState.value = UiState.Loading }
      .doOnSuccess { amazonTransactionResult ->
        validateResultOfPaymentLink(amazonTransactionResult)
      }
      .subscribe({}, { _ ->
        Log.i("Amazon", "error 1")
        _uiState.value = UiState.Error
      })
  }

  private fun createAmazonPayTransaction(
    chargePermissionId: String?,
    transactionBuilder: TransactionBuilder,
    amount: String,
    fiatCurrencySymbol: String,
    origin: String?
  ): Single<AmazonPayTransaction> {

    val price = AmazonPrice(value = amount, currency = fiatCurrencySymbol)
    return createAmazonPayTransactionUseCase(
      price = price,
      reference = transactionBuilder.orderReference,
      origin = origin,
      metadata = transactionBuilder.payload,
      sku = transactionBuilder.skuId,
      callbackUrl = transactionBuilder.callbackUrl,
      transactionType = transactionBuilder.type,
      referrerUrl = transactionBuilder.referrerUrl,
      packageName = transactionBuilder.domain,
      chargePermissionId = chargePermissionId,
      guestWalletId = transactionBuilder.guestWalletId,
    )
  }

  fun sendPaymentStartEvent(transactionBuilder: TransactionBuilder?) {
    analytics.sendPaymentConfirmationEvent(
      transactionBuilder?.domain, transactionBuilder?.skuId,
      transactionBuilder?.amount()
        .toString(), BillingAnalytics.PAYMENT_METHOD_AMAZON_PAY,
      transactionBuilder?.type, "BUY"
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

      else -> {
        Log.i("Amazon", "error 2")
        _uiState.value = UiState.Error
      }
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
          Log.i("Amazon", "error 3")
          _uiState.value = UiState.Error
          return
        }
      }
      CompositeDisposable().add(
        patchAmazonPayCheckoutSessionUseCase(
          amazonTransaction?.uid,
          amazonPayCheckoutRequest
        ).subscribe({}, {
          Log.i("Amazon", "error 4")
          _uiState.value = UiState.Error
        })
      )
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
        Log.i("Amazon", "error 5")
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
          analytics.sendPaymentSuccessEvent(
            packageName = transactionBuilder.domain,
            skuDetails = transaction.skuId,
            value = transaction.amount().toString(),
            purchaseDetails = BillingAnalytics.PAYMENT_METHOD_AMAZON_PAY,
            transactionType = transaction.type,
            txId = txId,
            valueUsd = transaction.amountUsd.toString()
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
          analytics.sendPaymentErrorWithDetailsAndRiskEvent(
            transaction.domain,
            transaction.skuId,
            transaction.amount().toString(),
            BillingAnalytics.PAYMENT_METHOD_AMAZON_PAY,
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
    amazonTransaction?.uid?.let { amazonUid ->
      getTransactionStatusUseCase(
        uid = amazonUid
      ).map {
        when (it.status) {
          Transaction.Status.COMPLETED -> {
            stopTransactionStatusTimer()
            successInfo = SuccessInfo(
              hash = it.hash,
              orderReference = null,
              purchaseUid = it.uid,
            )
            _uiState.value = UiState.Success
          }

          Transaction.Status.INVALID_TRANSACTION,
          Transaction.Status.FAILED,
          Transaction.Status.CANCELED,
          Transaction.Status.FRAUD -> {
            stopTransactionStatusTimer()
            Log.i("Amazon", "error 6")
            _uiState.value = UiState.Error
          }

          Transaction.Status.PENDING,
          Transaction.Status.PENDING_SERVICE_AUTHORIZATION,
          Transaction.Status.PROCESSING,
          Transaction.Status.PENDING_USER_PAYMENT,
          Transaction.Status.SETTLED -> {
          }
        }
      }.subscribe({}, { it.printStackTrace() })
    }

  }

  fun getSuccessBundle(
    transactionBuilder: TransactionBuilder?
  ) {
    if (transactionBuilder == null) {
      Log.i("Amazon", "error 7")
      _uiState.value = UiState.Error
      return
    }
    inAppPurchaseInteractor.savePreSelectedPaymentMethod(
      PaymentMethodsView.PaymentMethodId.AMAZONPAY.id
    )
    CompositeDisposable().add(
      createSuccessBundleUseCase(
        transactionBuilder.type,
        transactionBuilder.domain,
        transactionBuilder.skuId,
        successInfo?.purchaseUid,
        successInfo?.orderReference,
        successInfo?.hash,
        rxSchedulers.io
      ).doOnSuccess {
        _uiState.value = UiState.SendSuccessBundle(it.bundle)
      }.subscribeOn(rxSchedulers.main).observeOn(rxSchedulers.main).doOnError {
        Log.i("Amazon", "error 8")
        _uiState.value = UiState.Error
      }.subscribe({}, { it.printStackTrace() })
    )
  }

}