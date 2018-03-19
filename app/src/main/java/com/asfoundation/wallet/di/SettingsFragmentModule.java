package com.asfoundation.wallet.di;

import com.asfoundation.wallet.router.ManageWalletsRouter;
import dagger.Module;
import dagger.Provides;

@Module class SettingsFragmentModule {
  @Provides ManageWalletsRouter provideManageWalletsRouter() {
    return new ManageWalletsRouter();
  }
}
