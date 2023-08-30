package com.asfoundation.wallet.ui.settings.wallets

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.disposables.CompositeDisposable

@InstallIn(FragmentComponent::class)
@Module
class SettingsWalletsModule {

  @Provides
  fun providesSettingsWalletsPresenter(
    fragment: Fragment,
    navigator: SettingsWalletsNavigator
  ): SettingsWalletsPresenter {
    return SettingsWalletsPresenter(
      fragment as SettingsWalletsView, navigator,
      CompositeDisposable()
    )
  }
}