package com.asfoundation.wallet.my_wallets.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.home.usecases.ObserveDefaultWalletUseCase
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase

class MyWalletsViewModelFactory(private val balanceInteractor: BalanceInteractor,
                                private val walletsInteract: WalletsInteract,
                                private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
                                private val observeDefaultWalletUseCase: ObserveDefaultWalletUseCase) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return MyWalletsViewModel(balanceInteractor, walletsInteract, observeWalletInfoUseCase,
        observeDefaultWalletUseCase) as T
  }
}