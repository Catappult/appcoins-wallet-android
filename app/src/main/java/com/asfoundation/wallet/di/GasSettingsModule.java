package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.viewmodel.GasSettingsViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module public class GasSettingsModule {

  @Provides public GasSettingsViewModelFactory provideGasSettingsViewModelFactory(
      FindDefaultNetworkInteract findDefaultNetworkInteract) {
    return new GasSettingsViewModelFactory(findDefaultNetworkInteract);
  }
}
