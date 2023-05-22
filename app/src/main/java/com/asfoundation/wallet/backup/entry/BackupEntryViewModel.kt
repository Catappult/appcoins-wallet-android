package com.asfoundation.wallet.backup.entry

import com.appcoins.wallet.ui.arch.*
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.ui.arch.data.Async
import com.asfoundation.wallet.wallets.domain.WalletBalance
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase

sealed class BackupEntrySideEffect : SideEffect

data class BackupEntryState(
  val walletAddress: String,
  val balanceAsync: Async<Balance> = Async.Uninitialized,
) : ViewState

class BackupEntryViewModel(
  private val data: BackupEntryData,
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val currencyFormatUtils: CurrencyFormatUtils,
  private val rxSchedulers: RxSchedulers,
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
    getWalletInfoUseCase(data.walletAddress, cached = true, updateFiat = false)
      .map { walletInfo -> mapBalance(walletInfo.walletBalance) }
      .subscribeOn(rxSchedulers.io)
      .asAsyncToState(BackupEntryState::balanceAsync) {
        copy(balanceAsync = it)
      }
      .scopedSubscribe() { e ->
        e.printStackTrace()
      }
  }

  private fun mapBalance(walletBalance: WalletBalance): Balance {
    val balance = walletBalance.overallFiat
    return Balance(balance.symbol, currencyFormatUtils.formatCurrency(balance.amount))
  }
}