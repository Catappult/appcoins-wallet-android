package com.asfoundation.wallet.manage_wallets.bottom_sheet

import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.asfoundation.wallet.manage_wallets.bottom_sheet.ChangeActiveWalletBottomSheetSideEffect.*
import com.asfoundation.wallet.ui.wallets.WalletDetailsInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class ChangeActiveWalletBottomSheetSideEffect : SideEffect {
  object NavigateBack : ChangeActiveWalletBottomSheetSideEffect()
  object WalletChanged : ChangeActiveWalletBottomSheetSideEffect()
}

data class ChangeActiveWalletBottomSheetState(
    val walletNameAsync: Async<Unit> = Async.Uninitialized,
) : ViewState

@HiltViewModel
class ChangeActiveWalletBottomSheetViewModel @Inject constructor(
  private val walletDetailsInteractor: WalletDetailsInteractor
) :
  BaseViewModel<ChangeActiveWalletBottomSheetState, ChangeActiveWalletBottomSheetSideEffect>(initialState()) {

  companion object {
    fun initialState():ChangeActiveWalletBottomSheetState {
      return ChangeActiveWalletBottomSheetState()
    }
  }

  fun changeActiveWallet(wallet: String) {
    walletDetailsInteractor.setActiveWallet(wallet)
      .doOnComplete { sendSideEffect { WalletChanged } }
      .subscribe()
  }

}