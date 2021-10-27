package com.asfoundation.wallet.my_wallets.verify_picker

import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.ui.wallets.WalletBalance

sealed class VerifyPickerSideEffect : SideEffect {
}

data class VerifyPickerState(
    val walletBalance: WalletBalance
) : ViewState

class VerifyPickerDialogViewModel :
    BaseViewModel<ChangeActiveWalletState, ChangeActiveWalletSideEffect>(initialState(data)) {
}