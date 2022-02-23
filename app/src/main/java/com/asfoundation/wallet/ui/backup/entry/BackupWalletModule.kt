package com.asfoundation.wallet.ui.backup.entry

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.ui.backup.entry.BackupWalletFragment.Companion.WALLET_ADDRESS_KEY
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class BackupWalletModule {

  @Provides
  fun providesBackupWalletViewModelFactory(
    getWalletInfoUseCase: GetWalletInfoUseCase,
    currencyFormatUtils: CurrencyFormatUtils,
    data: BackupWalletData
  ): BackupWalletViewModelFactory {
    return BackupWalletViewModelFactory(
      data,
      getWalletInfoUseCase,
      currencyFormatUtils
    )
  }

  @Provides
  fun providesBackupWalletData(fragment: Fragment): BackupWalletData {
    fragment.requireArguments()
        .apply {
          return BackupWalletData(getString(WALLET_ADDRESS_KEY)!!)
        }
  }
}