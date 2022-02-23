package com.asfoundation.wallet.ui.backup.entry

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase

sealed class BackupWalletSideEffect : SideEffect

data class BackupWalletState(
  val walletAddress: String,
  val balanceAsync: Async<Balance> = Async.Uninitialized,
) : ViewState

class BackupWalletViewModel(
  private val data: BackupWalletData,
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val currencyFormatUtils: CurrencyFormatUtils,
) : BaseViewModel<BackupWalletState, BackupWalletSideEffect>(
  initialState(data)
) {

  companion object {
    fun initialState(data: BackupWalletData): BackupWalletState {
      return BackupWalletState(data.walletAddress)
    }
  }

  init {
    showBalance()
  }

  private fun showBalance() {
    getWalletInfoUseCase(data.walletAddress, cached = true, updateFiat = false)
      .map { walletInfo -> mapBalance(walletInfo) }
      .asAsyncToState(BackupWalletState::balanceAsync) {
        copy(balanceAsync = it)
      }
      .scopedSubscribe { e ->
        e.printStackTrace()
      }
  }

  private fun mapBalance(walletInfo: WalletInfo): Balance {
    val balance = walletInfo.walletBalance.overallFiat
    return Balance(balance.symbol, currencyFormatUtils.formatCurrency(balance.amount))
  }
}