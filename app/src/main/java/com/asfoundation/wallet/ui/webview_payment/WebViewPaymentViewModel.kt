package com.asfoundation.wallet.ui.webview_payment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.analytics.analytics.rewards.RewardsAnalytics
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.promocode.data.use_cases.VerifyAndSavePromoCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.analytics.PaymentMethodAnalyticsMapper
import com.asfoundation.wallet.backup.BackupNotificationUtils
import com.asfoundation.wallet.billing.paypal.usecases.CreateSuccessBundleUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.promotions.usecases.StartVipReferralPollingUseCase
import com.asfoundation.wallet.transactions.PerkBonusAndGamificationService
import com.asfoundation.wallet.ui.iab.IabInteract
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.asfoundation.wallet.ui.webview_login.usecases.FetchUserKeyUseCase
import com.asfoundation.wallet.ui.webview_payment.usecases.CreateWebViewPaymentOspUseCase
import com.asfoundation.wallet.ui.webview_payment.usecases.CreateWebViewPaymentSdkUseCase
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
  private val paymentAnalytics: PaymentMethodsAnalytics,
  private val verifyAndSavePromoCodeUseCase: VerifyAndSavePromoCodeUseCase,
  private val fetchUserKeyUseCase: FetchUserKeyUseCase,
  private val createWebViewPaymentSdkUseCase: CreateWebViewPaymentSdkUseCase,
  private val createWebViewPaymentOspUseCase: CreateWebViewPaymentOspUseCase,
  private val ewtObtainer: EwtAuthenticatorService,
  private var rewardsAnalytics: RewardsAnalytics,
  private val logger: Logger,
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
          logger.log(
            "createSuccessBundleAndFinish",
            "Success in createSuccessBundleAndFinish for WebViewPayment ${it.bundle.toString()}",
            true,
            true
          )
        }
        .subscribeOn(rxSchedulers.io)
        .observeOn(rxSchedulers.io)
        .doOnError {
          logger.log(
            "createSuccessBundleAndFinish",
            "onError in createSuccessBundleAndFinish for WebViewPayment ${it.message} ${it.stackTrace}",
            true,
            true
          )
          _uiState.value = UiState.Finish
        }
        .subscribe({}, {
          logger.log(
            "createSuccessBundleAndFinish",
            "onError subscribe in createSuccessBundleAndFinish for WebViewPayment ${it.message} ${it.stackTrace}",
            true,
            true
          )
          Log.i(TAG, "createSuccessBundleAndFinish: ${it.message}")
        })
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
    compositeDisposable.add(
      Single.just(transactionBuilder)
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
    compositeDisposable.add(
      iabInteract.getWalletAddress()
        .subscribeOn(rxSchedulers.io)
        .observeOn(rxSchedulers.io)
        .flatMap { startVipReferralPollingUseCase(Wallet(it)) }
        .doOnSuccess {
          PerkBonusAndGamificationService.buildService(context, it.address)
          _uiState.value = UiState.FinishActivity(bundle)
        }
        .doOnError { _uiState.value = UiState.FinishActivity(bundle) }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  fun handleBackupNotifications(bundle: Bundle, context: Context) {
    compositeDisposable.add(
      iabInteract.incrementAndValidateNotificationNeeded()
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
        .doOnError { _uiState.value = UiState.FinishActivity(bundle) }
        .subscribe({ }, { it.printStackTrace() })
    )
  }

  fun setPromoCode(promoCode: String) {
    CompositeDisposable().add(
      verifyAndSavePromoCodeUseCase(promoCode)
        .subscribeOn(rxSchedulers.io)
        .observeOn(rxSchedulers.io)
        .doOnSuccess {
          rewardsAnalytics.submitNewPromoCodeClickEvent(promoCode)
        }
        .doOnError {}
        .subscribe({ }, { it.printStackTrace() })
    )
  }

  fun sendRevenueEvent(transactionBuilder: TransactionBuilder) {
    compositeDisposable.add(
      Single.just(transactionBuilder)
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

  fun fetchUserKey(authToken: String, type: String, transaction: TransactionBuilder) {
    CompositeDisposable().add(
      fetchUserKeyUseCase(authToken)
        .doOnComplete { Log.d(TAG, "fetchUserKey: success") }
        .andThen(
          ewtObtainer.getEwtAuthenticationNoBearer()
        )
        .flatMap {
          when (type) {
            WebViewPaymentActivity.OSP_TRANSACTION -> createWebViewPaymentOspUseCase(
              transaction = transaction,
              appVersion = BuildConfig.VERSION_CODE.toString()
            )

            WebViewPaymentActivity.SDK_TRANSACTION -> createWebViewPaymentSdkUseCase(
              transaction = transaction,
              appVersion = BuildConfig.VERSION_CODE.toString()
            )

            else -> {
              _uiState.value = UiState.Finish
              Single.just("")
            }
          }
        }
        .subscribeOn(rxSchedulers.io)
        .observeOn(rxSchedulers.io)
        .subscribe({
          Log.d(TAG, "new URL generated: ${it}")
          _uiState.value = UiState.LoadUrl(it)
        }, {
          it.printStackTrace()
          logger.log(TAG, "error in fetchUserKey or createUrl: ${it.message}", it)
          _uiState.value = UiState.Finish
        })
    )
  }

  sealed class UiState {
    data class FinishWithBundle(val bundle: Bundle) : UiState()
    data class FinishActivity(val bundle: Bundle) : UiState()
    data object Finish : UiState()
    data object ShowPaymentMethods : UiState()
    data class LoadUrl(val url: String) : UiState()
  }
}