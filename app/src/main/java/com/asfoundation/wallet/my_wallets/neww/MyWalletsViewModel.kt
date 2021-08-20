package com.asfoundation.wallet.my_wallets.neww

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.home.usecases.ObserveDefaultWalletUseCase
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.balance.BalanceScreenModel
import com.asfoundation.wallet.ui.balance.BalanceVerificationModel
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.ui.wallets.WalletsModel
import io.reactivex.schedulers.Schedulers

object MyWalletsSideEffect : SideEffect

data class MyWalletsState(
    val walletsAsync: Async<WalletsModel> = Async.Uninitialized,
    val walletVerifiedAsync: Async<BalanceVerificationModel> = Async.Uninitialized,
    val balanceAsync: Async<BalanceScreenModel> = Async.Uninitialized,
    val walletCreationAsync: Async<Unit> = Async.Uninitialized,
    val backedUpOnceAsync: Async<Boolean> = Async.Uninitialized,
) : ViewState

class MyWalletsViewModel(
    private val balanceInteractor: BalanceInteractor,
    private val walletsInteract: WalletsInteract,
    private val observeDefaultWalletUseCase: ObserveDefaultWalletUseCase
) : BaseViewModel<MyWalletsState, MyWalletsSideEffect>(initialState()) {

  companion object {
    fun initialState(): MyWalletsState {
      return MyWalletsState()
    }
  }

  init {
    observeCurrentWallet()
  }

  fun refreshData() {
    fetchWallets()
    fetchWalletVerified()
    fetchBalance()
    observeHasBackedUpWallet()
  }

  private fun observeCurrentWallet() {
    observeDefaultWalletUseCase()
        .doOnNext { wallet ->
          val currentWalletModel = state.walletsAsync()
          if (currentWalletModel == null || currentWalletModel.currentWallet.walletAddress != wallet.address) {
            // Refresh data if our active wallet changed
            refreshData()
          }

        }
        .scopedSubscribe { e -> e.printStackTrace() }
  }

  private fun fetchWallets() {
    walletsInteract.getWalletsModel()
        .subscribeOn(Schedulers.io())
        .asAsyncToState { wallet -> copy(walletsAsync = wallet) }
        .repeatableScopedSubscribe(MyWalletsState::walletsAsync.name) { e ->
          e.printStackTrace()
        }
  }

  private fun fetchWalletVerified() {
    balanceInteractor.observeCurrentWalletVerified()
        .subscribeOn(Schedulers.io())
        .asAsyncToState { verification -> copy(walletVerifiedAsync = verification) }
        .repeatableScopedSubscribe(MyWalletsState::walletVerifiedAsync.name) { e ->
          e.printStackTrace()
        }
  }

  private fun fetchBalance() {
    balanceInteractor.requestTokenConversion()
        .subscribeOn(Schedulers.io())
        .asAsyncToState { balance -> copy(balanceAsync = balance) }
        .repeatableScopedSubscribe(MyWalletsState::balanceAsync.name) { e ->
          e.printStackTrace()
        }
  }

  private fun observeHasBackedUpWallet() {
    balanceInteractor.observeBackedUpOnce()
        .asAsyncToState { backedUpOnce -> copy(backedUpOnceAsync = backedUpOnce) }
        .repeatableScopedSubscribe(MyWalletsState::backedUpOnceAsync.name) { e ->
          e.printStackTrace()
        }
  }
}