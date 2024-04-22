package com.asfoundation.wallet.billing.mipay

import android.os.Bundle
import android.text.format.DateUtils
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.mutableStateOf
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.network.microservices.model.MiPayTransaction
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.paypal.usecases.CreateSuccessBundleUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetMiPayLinkUseCase
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetTransactionStatusUseCase
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import com.asfoundation.wallet.ui.iab.WebViewActivity
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


sealed class MiPayIABSideEffect : SideEffect {
  object ShowLoading : MiPayIABSideEffect()
  data class ShowError(val message: Int?) : MiPayIABSideEffect()
  object ShowSuccess : MiPayIABSideEffect()
  data class SendSuccessBundle(val bundle: Bundle) : MiPayIABSideEffect()
  object PaymentLinkSuccess : MiPayIABSideEffect()
}

data class MiPayIABState(
  val transaction: Async<MiPayTransaction> = Async.Uninitialized
) : ViewState

@HiltViewModel
class MiPayViewModel @Inject constructor(
  private val getTransactionStatusUseCase: GetTransactionStatusUseCase,
  private val rxSchedulers: RxSchedulers,
  private val analytics: BillingAnalytics,
  private val inAppPurchaseInteractor: InAppPurchaseInteractor,
  private val createSuccessBundleUseCase: CreateSuccessBundleUseCase,
  private val getMiPayLinkUseCase: GetMiPayLinkUseCase
) :
  BaseViewModel<MiPayIABState, MiPayIABSideEffect>(
    MiPayIABState()
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
  private var compositeDisposable: CompositeDisposable = CompositeDisposable()
  val transactionData = mutableStateOf<MiPayTransaction?>(null)

  val networkScheduler = rxSchedulers.io
  val viewScheduler = rxSchedulers.main

  data class SuccessInfo(
    val hash: String?,
    val orderReference: String?,
    val purchaseUid: String?,
  )

  var successInfo: SuccessInfo? = null

  fun getPaymentLink(
    transactionBuilder: TransactionBuilder,
    amount: String,
    fiatCurrency: String
  ) {
    getMiPayLinkUseCase(
      data = transactionBuilder,
      currency = fiatCurrency,
      packageName = transactionBuilder.domain,
      amount = amount,
      paymentType = PaymentType.MI_PAY.subTypes.first()
    ).asAsyncToState {
      copy(transaction = it)
    }.doOnSuccess {
      transactionData.value = it
      sendSideEffect { MiPayIABSideEffect.PaymentLinkSuccess }
    }.doOnSubscribe {
      sendSideEffect { MiPayIABSideEffect.ShowLoading }
    }
      .scopedSubscribe()
  }

  private fun startTransactionStatusTimer() {
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
        sendSideEffect { MiPayIABSideEffect.ShowError(R.string.unknown_error) }
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
            purchaseDetails = BillingAnalytics.PAYMENT_METHOD_MI_PAY,
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
        .toString(), BillingAnalytics.PAYMENT_METHOD_MI_PAY,
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
            BillingAnalytics.PAYMENT_METHOD_MI_PAY,
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
            sendSideEffect {
              successInfo = SuccessInfo(
                hash = it.hash,
                orderReference = null,
                purchaseUid = it.uid,
              )
              MiPayIABSideEffect.ShowSuccess
            }
          }

          Transaction.Status.INVALID_TRANSACTION,
          Transaction.Status.FAILED,
          Transaction.Status.CANCELED,
          Transaction.Status.FRAUD -> {
            stopTransactionStatusTimer()
            sendSideEffect {
              MiPayIABSideEffect.ShowError(
                R.string.purchase_error_wallet_block_code_403
              )
            }
          }

          Transaction.Status.PENDING,
          Transaction.Status.PENDING_SERVICE_AUTHORIZATION,
          Transaction.Status.PROCESSING,
          Transaction.Status.PENDING_USER_PAYMENT,
          Transaction.Status.SETTLED -> {
            sendSideEffect { MiPayIABSideEffect.ShowLoading }
          }
        }
      }.scopedSubscribe()
    }
  }

  fun getSuccessBundle(
    transactionBuilder: TransactionBuilder?
  ) {
    if (transactionBuilder == null) {
      sendSideEffect { MiPayIABSideEffect.ShowError(R.string.unknown_error) }
      return
    }
    inAppPurchaseInteractor.savePreSelectedPaymentMethod(
      PaymentMethodsView.PaymentMethodId.MI_PAY.id
    )
    createSuccessBundleUseCase(
      transactionBuilder.type,
      transactionBuilder.domain,
      transactionBuilder.skuId,
      successInfo?.purchaseUid,
      successInfo?.orderReference,
      successInfo?.hash,
      networkScheduler
    ).doOnSuccess {
      sendSideEffect { MiPayIABSideEffect.SendSuccessBundle(it.bundle) }
    }.subscribeOn(viewScheduler).observeOn(viewScheduler).doOnError {
      sendSideEffect { MiPayIABSideEffect.ShowError(R.string.unknown_error) }
    }.subscribe()
  }

  fun handleWebViewResult(result: ActivityResult) {
    when (result.resultCode) {
      WebViewActivity.SUCCESS -> {
        startTransactionStatusTimer()
      }

      WebViewActivity.FAIL,
      WebViewActivity.USER_CANCEL -> {
        analytics.sendPaymentErrorEvent(
          purchaseDetails = transactionData.toString(),
          transactionType = PaymentType.MI_PAY.subTypes.first(),
          packageName = "",
          skuDetails = "",
          value = "",
          errorCode = result.resultCode.toString(),
        )
        sendSideEffect { MiPayIABSideEffect.ShowError(R.string.unknown_error) }
      }
    }
  }

}