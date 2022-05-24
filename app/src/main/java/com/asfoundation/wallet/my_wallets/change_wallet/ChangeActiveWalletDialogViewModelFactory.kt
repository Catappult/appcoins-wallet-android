package com.asfoundation.wallet.my_wallets.change_wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.ui.wallets.WalletDetailsInteractor

class ChangeActiveWalletDialogViewModelFactory(
    val data: ChangeActiveWalletDialogData,
    val walletDetailsInteractor: WalletDetailsInteractor
) : ViewModelProvider.Factory {

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return ChangeActiveWalletDialogViewModel(data, walletDetailsInteractor) as T
  }
}