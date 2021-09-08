package com.asfoundation.wallet.my_wallets.create_wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.ui.wallets.WalletsInteract

class CreateWalletDialogViewModelFactory(
    val walletsInteract: WalletsInteract
) : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return CreateWalletDialogViewModel(walletsInteract) as T
  }
}