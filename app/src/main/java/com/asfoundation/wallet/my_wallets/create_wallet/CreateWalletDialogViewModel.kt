package com.asfoundation.wallet.my_wallets.create_wallet

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import javax.inject.Inject

object CreateWalletSideEffect : SideEffect

data class CreateWalletState(
  val walletCreationAsync: Async<Unit> = Async.Uninitialized
) : ViewState

@HiltViewModel
class CreateWalletDialogViewModel @Inject constructor(
  private val walletsInteract: WalletsInteract
) : BaseViewModel<CreateWalletState, CreateWalletSideEffect>(initialState()) {

  companion object {
    fun initialState(): CreateWalletState {
      return CreateWalletState()
    }
  }

  fun createNewWallet() {
    walletsInteract.createWallet()
      .asAsyncToState { copy(walletCreationAsync = it) }
      .repeatableScopedSubscribe(CreateWalletState::walletCreationAsync.name) { e ->
        e.printStackTrace()
      }
  }

  //Temporary bad code until this flow is refactored to the new design
  fun recoverWallet() {
    Completable.fromAction {}
      .asAsyncToState { copy(walletCreationAsync = it) }
      .scopedSubscribe()
  }
}