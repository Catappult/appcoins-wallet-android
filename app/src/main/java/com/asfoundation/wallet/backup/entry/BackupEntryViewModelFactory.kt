package com.asfoundation.wallet.backup.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appcoins.wallet.core.utils.common.RxSchedulers
import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase

class BackupEntryViewModelFactory(
  private val data: BackupEntryData,
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val currencyFormatUtils: CurrencyFormatUtils,
  private val rxSchedulers: RxSchedulers,
) :
  ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return BackupEntryViewModel(data, getWalletInfoUseCase, currencyFormatUtils, rxSchedulers) as T
  }
}
