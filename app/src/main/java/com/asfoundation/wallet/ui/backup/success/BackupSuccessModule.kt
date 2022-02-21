package com.asfoundation.wallet.ui.backup.success

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.disposables.CompositeDisposable

@InstallIn(FragmentComponent::class)
@Module
class BackupSuccessModule {

  @Provides
  fun providesBackupSuccessPresenter(data: BackupSuccessData,
                                     fragment: Fragment): BackupSuccessPresenter {
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