package com.asfoundation.wallet.my_wallets.more

import androidx.lifecycle.SavedStateHandle
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.home.usecases.ObserveDefaultWalletUseCase
import com.asfoundation.wallet.ui.wallets.WalletBalance
import com.asfoundation.wallet.ui.wallets.WalletDetailsInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

sealed class MoreDialogSideEffect : SideEffect {
  object NavigateBack : MoreDialogSideEffect()
}

data class MoreDialogState(
  val walletAddress: String,
  val totalFiatBalance: String,
  val appcoinsBalance: String,
  val creditsBalance: String,
  val ethereumBalance: String,
  val walletsAsync: Async<WalletsModel> = Async.Uninitialized,
  val walletInfoAsync: Async<WalletInfo> = Async.Uninitialized
) : ViewState

@HiltViewModel
class MoreDialogViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val walletsInteract: WalletsInteract,
  private val walletDetailsInteractor: WalletDetailsInteractor,
  private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
  private val observeDefaultWalletUseCase: ObserveDefaultWalletUseCase
) :
  BaseViewModel<MoreDialogState, MoreDialogSideEffect>(initialState(savedStateHandle)) {

  companion object {
    fun initialState(savedStateHandle: SavedStateHandle): MoreDialogState = MoreDialogState(
      savedStateHandle.get<String>(MoreDialogFragment.WALLET_ADDRESS_KEY)!!,
      savedStateHandle.get<String>(MoreDialogFragment.FIAT_BALANCE_KEY)!!,
      savedStateHandle.get<String>(MoreDialogFragment.APPC_BALANCE_KEY)!!,
      savedStateHandle.get<String>(MoreDialogFragment.CREDITS_BALANCE_KEY)!!,
      savedStateHandle.get<String>(MoreDialogFragment.ETHEREUM_BALANCE_KEY)!!,
    )
  }

  init {
    observeCurrentWallet()
  }

  fun refreshData(flushAsync: Boolean) {
    fetchWallets(flushAsync)
    fetchWalletInfo(flushAsync)
  }

  fun changeActiveWallet(wallet: WalletBalance) {
    walletDetailsInteractor.setActiveWallet(wallet.walletAddress)
      .doOnComplete { sendSideEffect { MoreDialogSideEffect.NavigateBack } }
      .scopedSubscribe { it.printStackTrace() }
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