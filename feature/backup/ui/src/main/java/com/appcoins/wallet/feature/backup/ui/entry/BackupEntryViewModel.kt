package com.appcoins.wallet.feature.backup.ui.entry

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class BackupEntrySideEffect : SideEffect

data class BackupEntryState(
    val balanceAsync: Async<Balance> = Async.Uninitialized,
) : ViewState

@HiltViewModel
class BackupEntryViewModel
@Inject
constructor(
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val currencyFormatUtils: CurrencyFormatUtils,
  private val dispatchers: Dispatchers,
) : NewBaseViewModel<BackupEntryState, BackupEntrySideEffect>(BackupEntryState()) {
  lateinit var walletAddress: String
  lateinit var walletName: String
  var password: String = ""
  val correctInputPassword = mutableStateOf(true)
  val showBottomSheet: MutableState<Boolean> = mutableStateOf(false)

  fun showBalance(walletAddress: String) {
    viewModelScope.launch {
      val walletInfo =
        withContext(dispatchers.io) { getWalletInfoUseCase(walletAddress, cached = true).await() }
      suspend { mapBalance(walletInfo.walletBalance) }
        .mapSuspendToAsync(BackupEntryState::balanceAsync) { copy(balanceAsync = it) }
    }
  }

  private fun mapBalance(walletBalance: WalletBalance): Balance {
    val balance = walletBalance.overallFiat
    return Balance(balance.symbol, currencyFormatUtils.formatCurrency(balance.amount))
  }

  fun showBottomSheet(show: Boolean = true) {
    showBottomSheet.value = show
  }
}
