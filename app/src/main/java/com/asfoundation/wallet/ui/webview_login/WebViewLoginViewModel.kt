package com.asfoundation.wallet.ui.webview_login

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.analytics.analytics.rewards.RewardsAnalytics
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.promocode.data.use_cases.VerifyAndSavePromoCodeUseCase
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
import com.asfoundation.wallet.ui.webview_login.usecases.FetchUserKeyUseCase
import com.asfoundation.wallet.verification.ui.paypal.VerificationPaypalViewModel.VerificationPaypalState
import com.wallet.appcoins.feature.support.data.SupportInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class WebViewLoginViewModel @Inject constructor(
  private val rxSchedulers: RxSchedulers,
  private val fetchUserKeyUseCase: FetchUserKeyUseCase,
  private val logger: Logger,
) : ViewModel() {

  private var _uiState = MutableStateFlow<UiState>(UiState.ShowPaymentMethods)
  var uiState: StateFlow<UiState> = _uiState

  private val compositeDisposable = CompositeDisposable()
  private val TAG = "WebViewModel"
  var runningCustomTab = false
  var isFirstRun: Boolean = true
  var webView: WebView? = null

  fun fetchUserKey(authToken: String) {
    CompositeDisposable().add(
      fetchUserKeyUseCase(authToken)
        .subscribeOn(rxSchedulers.io)
        .observeOn(rxSchedulers.io)
        .subscribe({
          _uiState.value = UiState.FinishActivity
        }, {
          it.printStackTrace() }
        )
    )
  }


  sealed class UiState {
    data object FinishActivity : UiState()
    data object ShowPaymentMethods : UiState()
  }
}