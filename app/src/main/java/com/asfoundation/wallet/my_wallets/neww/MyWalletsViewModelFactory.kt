package com.asfoundation.wallet.my_wallets.neww

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.ui.balance.BalanceInteractor

class MyWalletsViewModelFactory(private val balanceInteractor: BalanceInteractor) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return MyWalletsViewModel(balanceInteractor) as T
  }
}