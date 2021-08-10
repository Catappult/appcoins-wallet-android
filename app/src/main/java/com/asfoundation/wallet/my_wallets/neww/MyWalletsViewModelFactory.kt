package com.asfoundation.wallet.my_wallets.neww

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MyWalletsViewModelFactory : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return MyWalletsViewModel() as T
  }
}