package com.asfoundation.wallet.home.bottom_sheet

import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.appcoins.wallet.core.arch.NewBaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.core.utils.android_common.Log
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import com.asfoundation.wallet.ui.webview_login.usecases.GenerateWebLoginUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class HomeManageWalletBottomSheetSideEffect : SideEffect {
  object NavigateBack : HomeManageWalletBottomSheetSideEffect()
  data class OpenLogin(val url: String) : HomeManageWalletBottomSheetSideEffect()
}

data class HomeManageWalletBottomSheetState(
  val currentWalletAsync: Async<WalletInfo> = Async.Uninitialized
) : ViewState

@HiltViewModel
class HomeManageWalletBottomSheetViewModel
@Inject
constructor(
  private val dispatchers: Dispatchers,
  private val walletsEventSender: WalletsEventSender,
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val generateWebLoginUrlUseCase: GenerateWebLoginUrlUseCase,
) :
  NewBaseViewModel<HomeManageWalletBottomSheetState, HomeManageWalletBottomSheetSideEffect>(
    initialState()
  ) {

  companion object {
    fun initialState(): HomeManageWalletBottomSheetState {
      return HomeManageWalletBottomSheetState()
    }
  }

  private var compositeDisposable: CompositeDisposable = CompositeDisposable()

  fun onBackupClick() {
    viewModelScope.launch {
      val walletInfo =
        withContext(dispatchers.io) { getWalletInfoUseCase(null, cached = true).await() }
      suspend { walletInfo }
        .mapSuspendToAsync(HomeManageWalletBottomSheetState::currentWalletAsync) {
          copy(currentWalletAsync = it)
        }
    }
  }

  fun sendOpenBackupEvent() {
    walletsEventSender.sendCreateBackupEvent(
      WalletsAnalytics.ACTION_CREATE,
      WalletsAnalytics.CONTEXT_CARD,
      WalletsAnalytics.STATUS_SUCCESS
    )
  }

  fun getLoginUrl(): String {
    return generateWebLoginUrlUseCase()
      .doOnError { error -> Log.d("getLoginUrl", "Error: ${error.message}") }
      .blockingGet()
  }

}
