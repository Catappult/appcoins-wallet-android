package com.asfoundation.wallet.manage_wallets.bottom_sheet

import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletsInteract
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class ManageWalletNameBottomSheetSideEffect : SideEffect {
  object NavigateBack : ManageWalletNameBottomSheetSideEffect()
}

data class ManageWalletNameBottomSheetState(
    val walletNameAsync: Async<Unit> = Async.Uninitialized,
) : ViewState

@HiltViewModel
class ManageWalletNameBottomSheetViewModel @Inject constructor(
  private val walletsInteract: WalletsInteract
) :
  BaseViewModel<ManageWalletNameBottomSheetState, ManageWalletNameBottomSheetSideEffect>(initialState()) {

  companion object {
    fun initialState():ManageWalletNameBottomSheetState {
      return ManageWalletNameBottomSheetState()
    }
  }

  fun createWallet(name: String) {
    walletsInteract.createWallet(name)
      .asAsyncToState { copy(walletNameAsync = it) }
      .scopedSubscribe()
  }

}