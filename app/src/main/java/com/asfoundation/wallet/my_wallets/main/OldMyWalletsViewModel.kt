package com.asfoundation.wallet.my_wallets.main

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.home.usecases.ObserveDefaultWalletUseCase
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.balance.BalanceVerificationModel
import com.asfoundation.wallet.ui.wallets.WalletDetailsInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

object OldMyWalletsSideEffect : SideEffect

data class OldMyWalletsState(
  val walletsAsync: Async<WalletsModel> = Async.Uninitialized,
  val walletVerifiedAsync: Async<BalanceVerificationModel> = Async.Uninitialized,
  val walletInfoAsync: Async<WalletInfo> = Async.Uninitialized
) : ViewState

@HiltViewModel
class OldMyWalletsViewModel @Inject constructor(
  private val balanceInteractor: BalanceInteractor,
  private val walletsInteract: WalletsInteract,
  private val walletDetailsInteractor: WalletDetailsInteractor,
  private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
  private val observeDefaultWalletUseCase: ObserveDefaultWalletUseCase
) : BaseViewModel<OldMyWalletsState, OldMyWalletsSideEffect>(initialState()) {

  companion object {
    fun initialState(): OldMyWalletsState = OldMyWalletsState()
  }

  init {
    observeCurrentWallet()
  }

  /**
   * Flushing Asyncs means we want to induce loading, this makes sense in the case of changing
   * active wallet, but not when we just enter the screen and we simply want to keep the contents
   * up-to-date.
   */
  fun refreshData(flushAsync: Boolean) {
    fetchWallets(flushAsync)
    fetchWalletVerified(flushAsync)
    fetchWalletInfo(flushAsync)
  }

  private fun observeCurrentWallet() {
    observeDefaultWalletUseCase()
      .doOnNext { wallet ->
        val currentWalletAddress = state.walletInfoAsync()?.wallet
        if (currentWalletAddress == null || currentWalletAddress != wallet.address) {
          refreshData(flushAsync = true)
        }
      }
      .map { it.address }
      .distinctUntilChanged { old, new -> old == new }
      .flatMapCompletable { walletDetailsInteractor.setActiveWalletSupport(it) }
      .repeatableScopedSubscribe("ObserveCurrentWallet", Throwable::printStackTrace)
  }

  private fun fetchWallets(flushAsync: Boolean) {
    val retainValue = if (flushAsync) null else OldMyWalletsState::walletsAsync
    walletsInteract.observeWalletsModel()
      .subscribeOn(Schedulers.io())
      .asAsyncToState(retainValue) { wallet -> copy(walletsAsync = wallet) }
      .repeatableScopedSubscribe(OldMyWalletsState::walletsAsync.name) { e ->
        e.printStackTrace()
      }
  }

  private fun fetchWalletVerified(flushAsync: Boolean) {
    val retainValue = if (flushAsync) null else OldMyWalletsState::walletVerifiedAsync
    balanceInteractor.observeCurrentWalletVerified()
      .subscribeOn(Schedulers.io())
      .asAsyncToState(retainValue) { verification -> copy(walletVerifiedAsync = verification) }
      .repeatableScopedSubscribe(OldMyWalletsState::walletVerifiedAsync.name) { e ->
        e.printStackTrace()
      }
  }

  private fun fetchWalletInfo(flushAsync: Boolean) {
    val retainValue = if (flushAsync) null else OldMyWalletsState::walletInfoAsync
    observeWalletInfoUseCase(null, update = true, updateFiat = true)
      .asAsyncToState(retainValue) { balance -> copy(walletInfoAsync = balance) }
      .repeatableScopedSubscribe(OldMyWalletsState::walletInfoAsync.name) { e ->
        e.printStackTrace()
      }
  }
}