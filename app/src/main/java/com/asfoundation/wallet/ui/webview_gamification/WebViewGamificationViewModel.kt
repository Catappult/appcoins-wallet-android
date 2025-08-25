package com.asfoundation.wallet.ui.webview_gamification

import android.webkit.WebView
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.ui.webview_login.usecases.FetchUserKeyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class WebViewGamificationViewModel @Inject constructor(
  private val rxSchedulers: RxSchedulers,
  private val fetchUserKeyUseCase: FetchUserKeyUseCase,
  private val displayChatUseCase: DisplayChatUseCase,
  private val logger: Logger,
) : ViewModel() {

  private var _uiState = MutableStateFlow<UiState>(UiState.ShowPaymentMethods)
  var uiState: StateFlow<UiState> = _uiState

  private val compositeDisposable = CompositeDisposable()
  private val TAG = "WebViewModel"
  var runningCustomTab = false
  var isFirstRun: Boolean = true
  var webView: WebView? = null

  fun displayChat() {
    displayChatUseCase()
  }

  sealed class UiState {
    data object FinishActivity : UiState()
    data object FinishWithError : UiState()
    data object ShowPaymentMethods : UiState()
  }
}