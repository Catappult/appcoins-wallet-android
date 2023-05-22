package com.appcoins.wallet.feature.backup.ui.save_options

import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.backup.data.use_cases.BackupSuccessLogUseCase
import com.appcoins.wallet.feature.backup.data.use_cases.SendBackupToEmailUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class BackupSaveOptionsModule {

  @Provides
  fun providesBackupSaveOptionsViewModelFactory(
      data: BackupSaveOptionsData,
      sendBackupToEmailUseCase: SendBackupToEmailUseCase,
      backupSuccessLogUseCase: BackupSuccessLogUseCase,
      logger: Logger
  ): BackupSaveOptionsViewModelFactory {
    return BackupSaveOptionsViewModelFactory(
      data, sendBackupToEmailUseCase, backupSuccessLogUseCase,
      logger
    )
  }

  @Provides
  fun providesBackupSaveOptionsData(fragment: Fragment): BackupSaveOptionsData {
    fragment.requireArguments()
      .apply {
        return BackupSaveOptionsData(
          getString(BackupSaveOptionsFragment.WALLET_ADDRESS_KEY)!!,
          getString(BackupSaveOptionsFragment.PASSWORD_KEY)!!
        )
      }
  }
}