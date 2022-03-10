package com.asfoundation.wallet.ui.backup.skip

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class BackupSkipDialogModule {

  @Provides
  fun providesBackupSkipDialogNavigator(
    fragment: Fragment
  ): BackupSkipDialogNavigator {
    return BackupSkipDialogNavigator(fragment)
  }
}
