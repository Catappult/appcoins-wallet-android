package com.asfoundation.wallet.manage_wallets.bottom_sheet

import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async

sealed class ManageWalletBalanceBottomSheetSideEffect : SideEffect {
  object NavigateBack : ManageWalletBalanceBottomSheetSideEffect()
}

data class ManageWalletBalanceBottomSheetState(
  val walletNameAsync: Async<Unit> = Async.Uninitialized,
) : ViewState