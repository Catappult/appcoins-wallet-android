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
  fun providesBackupSuccessPresenter(fragment: Fragment): BackupSuccessPresenter {
    return BackupSuccessPresenter(fragment as BackupSuccessFragmentView, CompositeDisposable())
  }
}