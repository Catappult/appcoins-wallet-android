package com.asfoundation.wallet.manage_wallets.bottom_sheet

import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletsInteract
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.activeWalletAddress
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.inactiveWallets
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.ObserveWalletInfoUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletNameUseCase
import com.asfoundation.wallet.manage_wallets.ManageWalletViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

sealed class ManageWalletBalanceBottomSheetSideEffect : SideEffect {
  object NavigateBack : ManageWalletBalanceBottomSheetSideEffect()
}

data class ManageWalletBalanceBottomSheetState(
    val walletNameAsync: Async<Unit> = Async.Uninitialized,
) : ViewState

@HiltViewModel
class ManageWalletBalanceBottomSheetViewModel @Inject constructor(
) :
  BaseViewModel<ManageWalletBalanceBottomSheetState, ManageWalletBalanceBottomSheetSideEffect>(initialState()) {

  private val _uiState = MutableStateFlow<ManageWalletViewModel.UiState>(ManageWalletViewModel.UiState.Idle)
  var uiState: StateFlow<ManageWalletViewModel.UiState> = _uiState

  companion object {
    fun initialState():ManageWalletBalanceBottomSheetState {
      return ManageWalletBalanceBottomSheetState()
    }
  }

  sealed class UiState {
    object Loading : UiState()
    data class Success(
      val activeWalletInfo: WalletInfo,
      val inactiveWallets: List<WalletInfoSimple>
    ) : UiState()
  }

}