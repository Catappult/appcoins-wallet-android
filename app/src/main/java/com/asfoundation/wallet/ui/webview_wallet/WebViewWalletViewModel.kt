package com.asfoundation.wallet.ui.webview_wallet

import android.webkit.WebView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.ui.webview_login.usecases.FetchUserKeyUseCase
import com.wallet.appcoins.feature.support.data.SupportInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class WebViewWalletViewModel @Inject constructor(
  private val rxSchedulers: RxSchedulers,
  private val fetchUserKeyUseCase: FetchUserKeyUseCase,
  private val supportInteractor: SupportInteractor,
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
          //_uiState.value = UiState.FinishActivity
        }, {
          it.printStackTrace()
          logger.log(TAG, "error in fetchUserKey: ${it.message}", it)
          //_uiState.value = UiState.FinishWithError
        })
    )
  }

  fun showSupport() {
    compositeDisposable.add(
      supportInteractor.showSupport().subscribe({}, {})
    )
  }


  sealed class UiState {
    data object FinishActivity : UiState()
    data object FinishWithError : UiState()
    data object ShowPaymentMethods : UiState()
  }
}