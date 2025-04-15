package com.asfoundation.wallet.ui.webview_payment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.asfoundation.wallet.analytics.PaymentMethodAnalyticsMapper
import com.asfoundation.wallet.backup.BackupNotificationUtils
import com.asfoundation.wallet.billing.paypal.usecases.CreateSuccessBundleUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.promotions.usecases.StartVipReferralPollingUseCase
import com.asfoundation.wallet.transactions.PerkBonusAndGamificationService
import com.asfoundation.wallet.ui.iab.IabInteract
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.wallet.appcoins.feature.support.data.SupportInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class WebViewPaymentViewModel @Inject constructor(
  private val createSuccessBundleUseCase: CreateSuccessBundleUseCase,
  private val rxSchedulers: RxSchedulers,
  private val supportInteractor: SupportInteractor,
  private val iabInteract: IabInteract,
  private val inAppPurchaseInteractor: InAppPurchaseInteractor,
  private val startVipReferralPollingUseCase: StartVipReferralPollingUseCase,
  private val analytics: BillingAnalytics,
  private val paymentAnalytics: PaymentMethodsAnalytics
) : ViewModel() {

  private var _uiState = MutableStateFlow<UiState>(UiState.ShowPaymentMethods)
  var uiState: StateFlow<UiState> = _uiState

  private val compositeDisposable = CompositeDisposable()
  private val TAG = "WebViewModel"
  var runningCustomTab = false
  var isFirstRun: Boolean = true
  var webView: WebView? = null

  fun createSuccessBundleAndFinish(
    type: String,
    merchantName: String,
    sku: String,
    purchaseUid: String,
    orderReference: String,
    hash: String,
    paymentMethod: String,
    transactionBuilder: TransactionBuilder
  ) {
    compositeDisposable.add(
      createSuccessBundleUseCase(
        type = type,
        merchantName = merchantName,
        sku = sku,
        purchaseUid = purchaseUid,
        orderReference = orderReference,
        hash = hash,
        scheduler = rxSchedulers.io
      )
        .doOnSuccess {
          sendPaymentEvent(paymentMethod, transactionBuilder)
          _uiState.value = UiState.FinishWithBundle(it.bundle)
        }
        .subscribeOn(rxSchedulers.io)
        .observeOn(rxSchedulers.io)
        .doOnError {
          _uiState.value = UiState.Finish
        }
        .subscribe({}, { Log.i(TAG, "createSuccessBundleAndFinish: ${it.message}") })
    )
  }

  private fun sendPaymentEvent(paymentMethod: String, transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(
      Single.just(transactionBuilder)
      .subscribeOn(rxSchedulers.io)
      .subscribe { transaction ->
        stopTimingForPurchaseEvent(true, paymentMethod)
        analytics.sendPaymentEvent(
          transaction.domain,
          transaction.skuId,
          transaction.amount().toString(),
          PaymentMethodAnalyticsMapper.mapPaymentToAnalytics(paymentMethod),
          transaction.type
        )
      })
  }

  private fun stopTimingForPurchaseEvent(success: Boolean, paymentMethod: String) {
    val paymentMethodAnalytics = PaymentMethodAnalyticsMapper.mapPaymentToAnalytics(paymentMethod)
    paymentAnalytics.stopTimingForPurchaseEvent(paymentMethodAnalytics, success, false)
  }

  fun sendPaymentSuccessEvent(
    uid: String,
    paymentMethod: String,
    isStoredCard: Boolean,
    wasCvcRequired: Boolean,
    transactionBuilder: TransactionBuilder
  ) {
    compositeDisposable.add(
      Single.just(transactionBuilder)
        .observeOn(rxSchedulers.io)
        .doOnSuccess { transaction ->
          val mappedPaymentType = PaymentMethodAnalyticsMapper.mapPaymentToAnalytics(paymentMethod)
          analytics.sendPaymentSuccessEvent(
            packageName = transactionBuilder.domain,
            skuDetails = transaction.skuId,
            value = transaction.amount().toString(),
            purchaseDetails = paymentMethod,
            transactionType = transaction.type,
            txId = uid,
            valueUsd = transaction.amountUsd.toString(),
            isStoredCard =
              if (mappedPaymentType == PaymentMethodsAnalytics.PAYMENT_METHOD_CC)
                isStoredCard
              else null,
            wasCvcRequired =
              if (mappedPaymentType == PaymentMethodsAnalytics.PAYMENT_METHOD_CC)
                wasCvcRequired
              else null,
            isWebViewPayment = true,
          )
        }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  fun sendPaymentErrorEvent(
    errorCode: String,
    errorReason: String,
    paymentMethod: String,
    transactionBuilder: TransactionBuilder
  ) {
    compositeDisposable.add(Single.just(transactionBuilder)
      .observeOn(rxSchedulers.io)
      .doOnSuccess { transaction ->
        analytics.sendPaymentErrorWithDetailsAndRiskEvent(
          packageName = transaction.domain,
          skuDetails = transaction.skuId,
          value = transaction.amount().toString(),
          purchaseDetails = paymentMethod,
          transactionType = transaction.type,
          errorCode = errorCode,
          errorDetails = errorReason,
          riskRules = null,
          isWebViewPayment = true,
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  fun handlePerkNotifications(bundle: Bundle, context: Context) {
    compositeDisposable.add(iabInteract.getWalletAddress()
      .subscribeOn(rxSchedulers.io)
      .observeOn(rxSchedulers.io)
      .flatMap { startVipReferralPollingUseCase(Wallet(it)) }
      .doOnSuccess {
        PerkBonusAndGamificationService.buildService(context, it.address)
        _uiState.value = UiState.FinishActivity(bundle)
      }
      .doOnError {  _uiState.value = UiState.FinishActivity(bundle) }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  fun handleBackupNotifications(bundle: Bundle, context: Context) {
    compositeDisposable.add(iabInteract.incrementAndValidateNotificationNeeded()
      .subscribeOn(rxSchedulers.io)
      .observeOn(rxSchedulers.io)
      .doOnSuccess { notificationNeeded ->
        if (notificationNeeded.isNeeded) {
          BackupNotificationUtils.showBackupNotification(
            context = context,
            walletAddress = notificationNeeded.walletAddress
          )
        }
        _uiState.value = UiState.FinishActivity(bundle)
      }
      .doOnError {  _uiState.value = UiState.FinishActivity(bundle) }
      .subscribe({ }, { it.printStackTrace() })
    )
  }

  fun sendRevenueEvent(transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(Single.just(transactionBuilder)
      .doOnSuccess { transaction ->
        analytics.sendRevenueEvent(
          inAppPurchaseInteractor.convertToFiat(
            transaction.amount().toDouble(),
            BillingAnalytics.EVENT_REVENUE_CURRENCY
          )
            .subscribeOn(rxSchedulers.io)
            .blockingGet()
            .amount
            .setScale(2, BigDecimal.ROUND_UP)
            .toString()
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  fun showSupport() {
    compositeDisposable.add(
      supportInteractor.showSupport().subscribe({}, {})
    )
  }

  sealed class UiState {
    data class FinishWithBundle(val bundle: Bundle) : UiState()
    data class FinishActivity(val bundle: Bundle) : UiState()
    data object Finish : UiState()
    data object ShowPaymentMethods : UiState()
  }
}