package com.asfoundation.wallet.my_wallets.more

import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState

sealed class MoreDialogSideEffect : SideEffect

data class MoreDialogState(val walletAddress: String, val totalFiatBalance: String,
                           val appcoinsBalance: String, val creditsBalance: String,
                           val ethereumBalance: String, val showVerifyCard: Boolean,
                           val showDeleteWallet: Boolean) : ViewState

class MoreDialogViewModel(private val data: MoreDialogData) :
    BaseViewModel<MoreDialogState, MoreDialogSideEffect>(initialState(data)) {

  companion object {
    fun initialState(data: MoreDialogData): MoreDialogState {
      return MoreDialogState(data.walletAddress, data.totalFiatBalance, data.appcoinsBalance,
          data.creditsBalance, data.ethereumBalance, data.showVerifyCard, data.showDeleteWallet)
    }
  }
}