package com.asfoundation.wallet.my_wallets.neww

import com.appcoins.wallet.bdsbilling.WalletAddressModel
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.balance.BalanceScreenModel
import com.asfoundation.wallet.ui.balance.BalanceVerificationModel

object MyWalletsSideEffect : SideEffect

data class MyWalletsState(
    val walletAsync: Async<WalletAddressModel> = Async.Uninitialized,
    val walletVerifiedAsync: Async<BalanceVerificationModel> = Async.Uninitialized,
    val balanceAsync: Async<BalanceScreenModel> = Async.Uninitialized
) : ViewState

class MyWalletsViewModel(private val balanceInteractor: BalanceInteractor) :
    BaseViewModel<MyWalletsState, MyWalletsSideEffect>(initialState()) {

  companion object {
    fun initialState(): MyWalletsState {
      return MyWalletsState()
    }
  }

  init {
    fetchWallet()
    fetchWalletVerified()
    fetchBalance()
  }

  private fun fetchWallet() {
    balanceInteractor.getSignedCurrentWalletAddress()
        .asAsyncToState { wallet -> copy(walletAsync = wallet) }
        .scopedSubscribe { e -> e.printStackTrace() }
  }

  private fun fetchWalletVerified() {
    balanceInteractor.observeCurrentWalletVerified()
        .asAsyncToState { verification -> copy(walletVerifiedAsync = verification) }
        .scopedSubscribe { e -> e.printStackTrace() }
  }

  private fun fetchBalance() {
    balanceInteractor.requestTokenConversion()
        .asAsyncToState { balance -> copy(balanceAsync = balance) }
        .scopedSubscribe { e -> e.printStackTrace() }
  }

}