package com.appcoins.wallet.feature.backup.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase

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
