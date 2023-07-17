package com.asfoundation.wallet.manage_wallets.bottom_sheet

import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class ManageWalletBottomSheetSideEffect : SideEffect {
  object NavigateBack : ManageWalletBottomSheetSideEffect()
}

data class ManageWalletBottomSheetState(
  val currentWalletAsync: Async<Wallet> = Async.Uninitialized
) : ViewState

@HiltViewModel
class ManageWalletBottomSheetViewModel @Inject constructor() :
  BaseViewModel<ManageWalletBottomSheetState, ManageWalletBottomSheetSideEffect>(
    initialState()
  ) {

  companion object {
    fun initialState(): ManageWalletBottomSheetState {
      return ManageWalletBottomSheetState()
    }
  }

}