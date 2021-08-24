package com.asfoundation.wallet.my_wallets.create_wallet

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.my_wallets.main.MyWalletsState
import com.asfoundation.wallet.ui.wallets.WalletsInteract

object CreateWalletSideEffect : SideEffect

data class CreateWalletState(
    val walletCreationAsync: Async<Unit> = Async.Uninitialized
) : ViewState

class CreateWalletDialogViewModel(
    private val walletsInteract: WalletsInteract
) : BaseViewModel<CreateWalletState, CreateWalletSideEffect>(initialState()) {

  companion object {
    fun initialState(): CreateWalletState {
      return CreateWalletState()
    }
  }

  init {
    createNewWallet()
  }

  fun createNewWallet() {
    walletsInteract.createWallet()
        .asAsyncToState { copy(walletCreationAsync = it) }
        .repeatableScopedSubscribe(MyWalletsState::walletCreationAsync.name) { e ->
          e.printStackTrace()
        }
  }
}