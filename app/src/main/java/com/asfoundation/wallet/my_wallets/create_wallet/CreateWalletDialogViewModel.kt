package com.asfoundation.wallet.my_wallets.create_wallet

import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletsInteract
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
}
