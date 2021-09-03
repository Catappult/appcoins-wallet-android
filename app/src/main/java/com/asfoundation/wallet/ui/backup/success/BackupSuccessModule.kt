package com.asfoundation.wallet.ui.backup.success

import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class BackupSuccessModule {

  @Provides
  fun providesBackupSuccessPresenter(fragment: BackupSuccessFragment): BackupSuccessPresenter {
    return BackupSuccessPresenter(fragment as BackupSuccessFragmentView, CompositeDisposable())
  }
}