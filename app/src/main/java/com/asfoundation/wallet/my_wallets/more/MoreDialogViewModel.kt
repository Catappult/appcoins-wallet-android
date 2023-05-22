package com.asfoundation.wallet.my_wallets.more

import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletsInteract
import com.asfoundation.wallet.ui.wallets.WalletDetailsInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

sealed class MoreDialogSideEffect : SideEffect {
  object NavigateBack : MoreDialogSideEffect()
}

data class MoreDialogStateItem constructor(
  val isSelected: Boolean,
  val walletName: String,
  val walletAddress: String,
  val fiatBalance: String
) {
  constructor(walletAddress: String, walletInfoSimple: WalletInfoSimple) : this(
    walletAddress == walletInfoSimple.walletAddress,
      walletInfoSimple.walletName,
      walletInfoSimple.walletAddress,
      walletInfoSimple.balance.symbol + currencyFormatUtils.formatCurrency(walletInfoSimple.balance.amount)
  )

  companion object {
    private val currencyFormatUtils = CurrencyFormatUtils()
  }
}

data class MoreDialogState(
  val walletAddress: String,
  val totalFiatBalance: String,
  val appcoinsBalance: String,
  val creditsBalance: String,
  val ethereumBalance: String,
  val walletsAsync: Async<List<MoreDialogStateItem>> = Async.Uninitialized
) : ViewState

@HiltViewModel
class MoreDialogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val walletsInteract: WalletsInteract,
    private val walletDetailsInteractor: WalletDetailsInteractor,
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
    refreshData()
  }

  fun refreshData() {
    walletsInteract.observeWalletsModel()
      .subscribeOn(Schedulers.io())
      .map { walletsModel ->
        walletsModel.wallets.map { MoreDialogStateItem(state.walletAddress, it) }
      }
      .asAsyncToState(MoreDialogState::walletsAsync) { wallets -> copy(walletsAsync = wallets) }
      .repeatableScopedSubscribe(MoreDialogState::walletsAsync.name) { e ->
        e.printStackTrace()
      }
  }

  fun changeActiveWallet(wallet: String) {
    walletDetailsInteractor.setActiveWallet(wallet)
      .doOnComplete { sendSideEffect { MoreDialogSideEffect.NavigateBack } }
      .scopedSubscribe { it.printStackTrace() }
  }
}