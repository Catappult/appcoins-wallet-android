package com.asfoundation.wallet.my_wallets.change

import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.ui.wallets.WalletBalance
import com.asfoundation.wallet.ui.wallets.WalletDetailsInteractor

sealed class ChangeActiveWalletSideEffect : SideEffect {
  object NavigateBack : ChangeActiveWalletSideEffect()
}

data class ChangeActiveWalletState(
    val walletBalance: WalletBalance
) : ViewState

class ChangeActiveWalletDialogViewModel(
    val data: ChangeActiveWalletDialogData,
    val walletDetailsInteractor: WalletDetailsInteractor
) : BaseViewModel<ChangeActiveWalletState, ChangeActiveWalletSideEffect>(initialState(data)) {

  companion object {
    fun initialState(data: ChangeActiveWalletDialogData): ChangeActiveWalletState {
      return ChangeActiveWalletState(data.walletBalance)
    }
  }

  fun changeActiveWallet() {
    walletDetailsInteractor.setActiveWallet(data.walletBalance.walletAddress)
        .doOnComplete { sendSideEffect { ChangeActiveWalletSideEffect.NavigateBack } }
        .scopedSubscribe { it.printStackTrace() }
  }
}