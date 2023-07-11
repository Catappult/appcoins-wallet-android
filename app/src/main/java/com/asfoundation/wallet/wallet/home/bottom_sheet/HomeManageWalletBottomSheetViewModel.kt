package com.asfoundation.wallet.wallet.home.bottom_sheet

import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletsInteract
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class HomeManageWalletBottomSheetSideEffect : SideEffect {
  object NavigateBack : HomeManageWalletBottomSheetSideEffect()
}

data class HomeManageWalletBottomSheetState(
    val walletAsync: Async<Unit> = Async.Uninitialized,
) : ViewState

@HiltViewModel
class HomeManageWalletBottomSheetViewModel @Inject constructor(
  private val walletsInteract: WalletsInteract
) :
  BaseViewModel<HomeManageWalletBottomSheetState, HomeManageWalletBottomSheetSideEffect>(
    initialState()
  ) {

  companion object {
    fun initialState(): HomeManageWalletBottomSheetState {
      return HomeManageWalletBottomSheetState()
    }
  }
}