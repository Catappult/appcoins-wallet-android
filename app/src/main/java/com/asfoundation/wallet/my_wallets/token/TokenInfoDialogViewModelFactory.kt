package com.asfoundation.wallet.my_wallets.token

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TokenInfoDialogViewModelFactory(
    val data: TokenInfoDialogData,
) : ViewModelProvider.Factory {

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return TokenInfoDialogViewModel(data) as T
  }
}