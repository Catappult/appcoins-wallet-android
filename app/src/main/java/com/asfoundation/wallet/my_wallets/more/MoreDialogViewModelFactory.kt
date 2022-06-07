package com.asfoundation.wallet.my_wallets.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.home.usecases.ObserveDefaultWalletUseCase
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase

class MoreDialogViewModelFactory(
  val data: MoreDialogData,
  private val walletsInteract: WalletsInteract,
  private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
  private val observeDefaultWalletUseCase: ObserveDefaultWalletUseCase
) : ViewModelProvider.Factory {

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    @Suppress("UNCHECKED_CAST")
    return MoreDialogViewModel(
      data,
      walletsInteract,
      observeWalletInfoUseCase,
      observeDefaultWalletUseCase
    ) as T
  }
}