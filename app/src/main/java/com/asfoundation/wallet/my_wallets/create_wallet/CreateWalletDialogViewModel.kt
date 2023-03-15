package com.asfoundation.wallet.my_wallets.create_wallet

import com.appcoins.wallet.ui.arch.Async
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import javax.inject.Inject

object CreateWalletSideEffect : com.appcoins.wallet.ui.arch.SideEffect

data class CreateWalletState(
  val walletCreationAsync: com.appcoins.wallet.ui.arch.Async<Unit> = com.appcoins.wallet.ui.arch.Async.Uninitialized
) : com.appcoins.wallet.ui.arch.ViewState

@HiltViewModel
class CreateWalletDialogViewModel @Inject constructor(
  private val walletsInteract: WalletsInteract
) : com.appcoins.wallet.ui.arch.BaseViewModel<CreateWalletState, CreateWalletSideEffect>(initialState()) {

  companion object {
    fun initialState(): CreateWalletState = CreateWalletState()
  }

  fun createNewWallet(fromOnBoarding: Boolean) {
    walletsInteract.createWallet(if (fromOnBoarding) "Main Wallet" else null)
      .asAsyncToState { copy(walletCreationAsync = it) }
      .repeatableScopedSubscribe(
        CreateWalletState::walletCreationAsync.name,
        Throwable::printStackTrace
      )
  }

  //Temporary bad code until this flow is refactored to the new design
  fun recoverWallet() {
    Completable.fromAction {}
      .asAsyncToState { copy(walletCreationAsync = it) }
      .scopedSubscribe()
  }
}
