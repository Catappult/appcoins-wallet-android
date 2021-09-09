package com.asfoundation.wallet.my_wallets.main

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
import io.reactivex.subjects.BehaviorSubject

object MyWalletsSideEffect : SideEffect

data class MyWalletsState(
    val walletsAsync: Async<WalletsModel> = Async.Uninitialized,
    val walletVerifiedAsync: Async<BalanceVerificationModel> = Async.Uninitialized,
    val balanceAsync: Async<BalanceScreenModel> = Async.Uninitialized,
    val backedUpOnceAsync: Async<Boolean> = Async.Uninitialized,
) : ViewState

class MyWalletsViewModel(
    private val balanceInteractor: BalanceInteractor,
    private val walletsInteract: WalletsInteract,
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
          val currentWalletModel = state.walletsAsync()
          if (currentWalletModel == null || currentWalletModel.currentWallet.walletAddress != wallet.address) {
            // Full refresh data if our active wallet changed (meaning we flush our Async streams)
            // triggering Async.Loading
            fetchWallets()
            fetchWalletVerified()
            fetchBalance()
            observeHasBackedUpWallet()
          }
        }
        .scopedSubscribe { e -> e.printStackTrace() }
  }

  private fun fetchWallets() {
    softRefreshSubject
        .switchMap {
          walletsInteract.getWalletsModel()
              .subscribeOn(Schedulers.io())
              .toObservable()
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

  private fun fetchBalance() {
    softRefreshSubject
        .switchMap {
          balanceInteractor.requestTokenConversion()
              .subscribeOn(Schedulers.io())
        }
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