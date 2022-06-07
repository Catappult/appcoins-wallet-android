package com.asfoundation.wallet.my_wallets.more

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.home.usecases.ObserveDefaultWalletUseCase
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import io.reactivex.schedulers.Schedulers

sealed class MoreDialogSideEffect : SideEffect

data class MoreDialogState(
  val walletAddress: String,
  val totalFiatBalance: String,
  val appcoinsBalance: String,
  val creditsBalance: String,
  val ethereumBalance: String,
  val walletsAsync: Async<WalletsModel> = Async.Uninitialized,
  val walletInfoAsync: Async<WalletInfo> = Async.Uninitialized
) : ViewState

class MoreDialogViewModel(
  data: MoreDialogData,
  private val walletsInteract: WalletsInteract,
  private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
  private val observeDefaultWalletUseCase: ObserveDefaultWalletUseCase
) :
  BaseViewModel<MoreDialogState, MoreDialogSideEffect>(initialState(data)) {

  companion object {
    fun initialState(data: MoreDialogData): MoreDialogState = MoreDialogState(
      data.walletAddress,
      data.totalFiatBalance,
      data.appcoinsBalance,
      data.creditsBalance,
      data.ethereumBalance
    )
  }

  init {
    observeCurrentWallet()
  }

  fun refreshData(flushAsync: Boolean) {
    fetchWallets(flushAsync)
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
      .repeatableScopedSubscribe("ObserveCurrentWallet") { e -> e.printStackTrace() }
  }

  private fun fetchWallets(flushAsync: Boolean) {
    val retainValue = if (flushAsync) null else MoreDialogState::walletsAsync
    walletsInteract.observeWalletsModel()
      .subscribeOn(Schedulers.io())
      .asAsyncToState(retainValue) { wallet -> copy(walletsAsync = wallet) }
      .repeatableScopedSubscribe(MoreDialogState::walletsAsync.name) { e ->
        e.printStackTrace()
      }
  }

  private fun fetchWalletInfo(flushAsync: Boolean) {
    val retainValue = if (flushAsync) null else MoreDialogState::walletInfoAsync
    observeWalletInfoUseCase(null, update = true, updateFiat = true)
      .asAsyncToState(retainValue) { balance -> copy(walletInfoAsync = balance) }
      .repeatableScopedSubscribe(MoreDialogState::walletInfoAsync.name) { e ->
        e.printStackTrace()
      }
  }
}