package com.asfoundation.wallet.my_wallets.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MoreDialogViewModelFactory(val data: MoreDialogData) : ViewModelProvider.Factory {

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return MoreDialogViewModel(data) as T
  }
}