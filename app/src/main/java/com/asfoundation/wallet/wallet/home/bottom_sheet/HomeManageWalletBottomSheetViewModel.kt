package com.asfoundation.wallet.wallet.home.bottom_sheet

import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.appcoins.wallet.core.arch.NewBaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class HomeManageWalletBottomSheetSideEffect : SideEffect {
  object NavigateBack : HomeManageWalletBottomSheetSideEffect()
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
  private val getWalletInfoUseCase: GetWalletInfoUseCase
) :
  NewBaseViewModel<HomeManageWalletBottomSheetState, HomeManageWalletBottomSheetSideEffect>(
    initialState()
  ) {

  companion object {
    fun initialState(): HomeManageWalletBottomSheetState {
      return HomeManageWalletBottomSheetState()
    }
  }

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
}
