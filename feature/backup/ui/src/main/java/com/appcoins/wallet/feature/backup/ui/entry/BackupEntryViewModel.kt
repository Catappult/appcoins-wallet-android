package com.appcoins.wallet.feature.backup.ui.entry

import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.arch.NewBaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.backup.data.Balance
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletBalance
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext

sealed class BackupEntrySideEffect : SideEffect

data class BackupEntryState(
  val walletAddress: String,
  val balanceAsync: Async<Balance> = Async.Uninitialized,
) : ViewState

class BackupEntryViewModel(
  private val data: BackupEntryData,
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val currencyFormatUtils: CurrencyFormatUtils,
  private val dispatchers: Dispatchers,
) : NewBaseViewModel<BackupEntryState, BackupEntrySideEffect>(
  initialState(data)
) {

  companion object {
    fun initialState(data: BackupEntryData): BackupEntryState {
      return BackupEntryState(data.walletAddress)
    }
  }

  init {
    showBalance()
  }

  private fun showBalance() {
    viewModelScope.launch {
      val walletInfo = withContext(dispatchers.io) {
        getWalletInfoUseCase(data.walletAddress, cached = true).await()
      }
      suspend { mapBalance(walletInfo.walletBalance) }.mapSuspendToAsync(
        BackupEntryState::balanceAsync
      ) { copy(balanceAsync = it) }
    }
  }

  private fun mapBalance(walletBalance: WalletBalance): Balance {
    val balance = walletBalance.overallFiat
    return Balance(
      balance.symbol,
      currencyFormatUtils.formatCurrency(balance.amount)
    )
  }
}