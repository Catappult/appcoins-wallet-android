package com.asfoundation.wallet.backup.entry

import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.asfoundation.wallet.backup.entry.BackupEntryFragment.Companion.WALLET_ADDRESS_KEY
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class BackupEntryModule {

  @Provides
  fun providesBackupEntryViewModelFactory(
      getWalletInfoUseCase: GetWalletInfoUseCase,
      currencyFormatUtils: CurrencyFormatUtils,
      data: BackupEntryData,
      dispatchers: Dispatchers,
  ): BackupEntryViewModelFactory {
    return BackupEntryViewModelFactory(
        data,
        getWalletInfoUseCase,
        currencyFormatUtils,
        dispatchers
    )
  }

  @Provides
  fun providesBackupEntryData(fragment: Fragment): BackupEntryData {
    fragment.requireArguments()
        .apply {
          return BackupEntryData(getString(WALLET_ADDRESS_KEY)!!)
        }
  }
}