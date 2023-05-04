package com.asfoundation.wallet.backup.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase

class BackupEntryViewModelFactory(
    private val data: BackupEntryData,
    private val getWalletInfoUseCase: GetWalletInfoUseCase,
    private val currencyFormatUtils: CurrencyFormatUtils,
    private val dispatchers: Dispatchers,
) :
    ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return BackupEntryViewModel(data, getWalletInfoUseCase, currencyFormatUtils, dispatchers) as T
  }
}
