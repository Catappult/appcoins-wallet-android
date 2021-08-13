package com.asfoundation.wallet.my_wallets.neww

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract

class MyWalletsViewModelFactory(private val balanceInteractor: BalanceInteractor,
                                private val walletsInteract: WalletsInteract) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return MyWalletsViewModel(balanceInteractor, walletsInteract) as T
  }
}