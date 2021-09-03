package com.asfoundation.wallet.ui.settings.wallets

import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class SettingsWalletsModule {

  @Provides
  fun providesSettingsWalletsPresenter(fragment: SettingsWalletsFragment,
                                       navigator: SettingsWalletsNavigator): SettingsWalletsPresenter {
    return SettingsWalletsPresenter(fragment as SettingsWalletsView, navigator,
        CompositeDisposable())
  }

  @Provides
  fun providesSettingsWalletsNavigator(
      fragment: SettingsWalletsFragment): SettingsWalletsNavigator {
    return SettingsWalletsNavigator(fragment.requireFragmentManager())
  }
}