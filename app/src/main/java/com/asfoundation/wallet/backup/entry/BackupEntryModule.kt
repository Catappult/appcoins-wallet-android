package com.asfoundation.wallet.backup.entry

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.backup.entry.BackupEntryFragment.Companion.WALLET_ADDRESS_KEY
import com.asfoundation.wallet.base.RxSchedulers
import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils
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
    rxSchedulers: RxSchedulers,
  ): BackupEntryViewModelFactory {
    return BackupEntryViewModelFactory(
      data,
      getWalletInfoUseCase,
      currencyFormatUtils,
      rxSchedulers
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