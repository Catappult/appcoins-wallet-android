package com.asfoundation.wallet.manage_wallets.bottom_sheet

import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletsInteract
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletNameUseCase
import com.asfoundation.wallet.manage_wallets.bottom_sheet.ManageWalletNameBottomSheetSideEffect.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class ManageWalletNameBottomSheetSideEffect : SideEffect {
  object NavigateBack : ManageWalletNameBottomSheetSideEffect()
  object WalletCreated : ManageWalletNameBottomSheetSideEffect()
}

data class ManageWalletNameBottomSheetState(
    val walletNameAsync: Async<Unit> = Async.Uninitialized,
) : ViewState

@HiltViewModel
class ManageWalletNameBottomSheetViewModel @Inject constructor(
  private val walletsInteract: WalletsInteract,
  private val updateWalletNameUseCase: UpdateWalletNameUseCase
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
      .doOnComplete { sendSideEffect { WalletCreated } }
      .scopedSubscribe()
  }

  fun setWalletName(wallet: String, name: String) {
    updateWalletNameUseCase(wallet, name)
      .doOnComplete { sendSideEffect { NavigateBack } }
      .scopedSubscribe()
  }

}