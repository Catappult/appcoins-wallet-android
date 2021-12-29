package com.asfoundation.wallet.ui.backup.success

import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class BackupSuccessModule {

  @Provides
  fun providesBackupSuccessPresenter(data: BackupSuccessData,
                                     fragment: BackupSuccessFragment): BackupSuccessPresenter {
    return BackupSuccessPresenter(data, fragment as BackupSuccessFragmentView,
        CompositeDisposable())
  }

  @Provides
  fun providesBackupSuccessDataData(
      fragment: BackupSuccessFragment): BackupSuccessData {
    fragment.requireArguments()
        .apply {
          return BackupSuccessData(
              getBoolean(BackupSuccessFragment.EMAIL_KEY))
        }
  }
}