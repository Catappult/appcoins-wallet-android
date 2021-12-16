package com.asfoundation.wallet.my_wallets.main

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.home.usecases.ObserveDefaultWalletUseCase
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.balance.BalanceVerificationModel
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

object MyWalletsSideEffect : SideEffect

data class MyWalletsState(
    val walletsAsync: Async<WalletsModel> = Async.Uninitialized,
    val walletVerifiedAsync: Async<BalanceVerificationModel> = Async.Uninitialized,
    val walletInfoAsync: Async<WalletInfo> = Async.Uninitialized,
    val backedUpOnceAsync: Async<Boolean> = Async.Uninitialized,
) : ViewState

class MyWalletsViewModel(
    private val balanceInteractor: BalanceInteractor,
    private val walletsInteract: WalletsInteract,
    private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
    private val observeDefaultWalletUseCase: ObserveDefaultWalletUseCase
) : BaseViewModel<MyWalletsState, MyWalletsSideEffect>(initialState()) {

  private val softRefreshSubject = BehaviorSubject.createDefault(Unit)

  companion object {
    fun initialState(): MyWalletsState {
      return MyWalletsState()
    }
  }

  init {
    observeCurrentWallet()
  }

  fun refreshData() {
    // Soft refresh data (meaning we DON'T flush our Async streams)
    // This way we can avoid flickering since we don't deal with Async.Loading with no previous value
    softRefreshSubject.onNext(Unit)
  }

  private fun observeCurrentWallet() {
    observeDefaultWalletUseCase()
        .doOnNext { wallet ->
          val currentWalletAddress = state.walletInfoAsync()?.wallet
          if (currentWalletAddress == null || currentWalletAddress != wallet.address) {
            // Full refresh data if our active wallet changed (meaning we flush our Async streams)
            // triggering Async.Loading
            fetchWallets()
            fetchWalletVerified()
            fetchWalletInfo()
            observeHasBackedUpWallet()
          }
        }
        .repeatableScopedSubscribe("ObserveCurrentWallet") { e -> e.printStackTrace() }
  }

  private fun fetchWallets() {
    softRefreshSubject
        .switchMap {
          walletsInteract.observeWalletsModel()
              .subscribeOn(Schedulers.io())
        }
        .asAsyncToState { wallet -> copy(walletsAsync = wallet) }
        .repeatableScopedSubscribe(MyWalletsState::walletsAsync.name) { e ->
          e.printStackTrace()
        }
  }

  private fun fetchWalletVerified() {
    softRefreshSubject
        .switchMap {
          balanceInteractor.observeCurrentWalletVerified()
              .subscribeOn(Schedulers.io())
        }
        .asAsyncToState { verification -> copy(walletVerifiedAsync = verification) }
        .repeatableScopedSubscribe(MyWalletsState::walletVerifiedAsync.name) { e ->
          e.printStackTrace()
        }
  }

  private fun fetchWalletInfo() {
    softRefreshSubject
        .switchMap { observeWalletInfoUseCase(null, update = true, updateFiat = true) }
        .asAsyncToState { balance -> copy(walletInfoAsync = balance) }
        .repeatableScopedSubscribe(MyWalletsState::walletInfoAsync.name) { e ->
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