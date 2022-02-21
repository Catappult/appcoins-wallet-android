package com.asfoundation.wallet.ui.backup.creation

import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.ui.backup.use_cases.BackupSuccessLogUseCase
import com.asfoundation.wallet.ui.backup.use_cases.SendBackupToEmailUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class BackupCreationModule {

  @Provides
  fun providesBackupCreationViewModelFactory(
      data: BackupCreationData,
      sendBackupToEmailUseCase: SendBackupToEmailUseCase,
      backupSuccessLogUseCase: BackupSuccessLogUseCase,
      logger: Logger): BackupCreationViewModelFactory {
    return BackupCreationViewModelFactory(data, sendBackupToEmailUseCase, backupSuccessLogUseCase,
        logger)
  }

  @Provides
  fun providesBackupCreationData(fragment: BackupCreationFragment): BackupCreationData {
    fragment.requireArguments()
        .apply {
          return BackupCreationData(getString(BackupCreationFragment.WALLET_ADDRESS_KEY)!!,
              getString(BackupCreationFragment.PASSWORD_KEY)!!)
        }
  }

  @Provides
  fun providesBackupCreationNavigator(fragment: BackupCreationFragment): BackupCreationNavigator {
    return BackupCreationNavigator(fragment.requireFragmentManager())
  }
}