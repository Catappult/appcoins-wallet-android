package com.appcoins.wallet.feature.backup.ui.entry

import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.feature.backup.ui.entry.BackupEntryFragment.Companion.WALLET_ADDRESS_KEY
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
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
      dispatchers: Dispatchers,
  ): BackupEntryViewModelFactory {
    return BackupEntryViewModelFactory(
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