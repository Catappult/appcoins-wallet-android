package com.asfoundation.wallet.backup.entry

import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.asfoundation.wallet.wallets.domain.WalletBalance
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
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
) : BaseViewModel<BackupEntryState, BackupEntrySideEffect>(
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
        getWalletInfoUseCase(data.walletAddress, cached = true, updateFiat = false).await()
      }
      suspend { mapBalance(walletInfo.walletBalance) }.mapAsyncToState(
          BackupEntryState::balanceAsync) { copy(balanceAsync = it) }
    }
  }

  private fun mapBalance(walletBalance: WalletBalance): Balance {
    val balance = walletBalance.overallFiat
    return Balance(balance.symbol, currencyFormatUtils.formatCurrency(balance.amount))
  }
}