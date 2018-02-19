package com.asf.wallet.di;

import com.asf.wallet.interact.FindDefaultNetworkInteract;
import com.asf.wallet.repository.EthereumNetworkRepositoryType;
import com.asf.wallet.viewmodel.GasSettingsViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module public class GasSettingsModule {

  @Provides public GasSettingsViewModelFactory provideGasSettingsViewModelFactory(
      FindDefaultNetworkInteract findDefaultNetworkInteract) {
    return new GasSettingsViewModelFactory(findDefaultNetworkInteract);
  }

  @Provides FindDefaultNetworkInteract provideFindDefaultNetworkInteract(
      EthereumNetworkRepositoryType ethereumNetworkRepositoryType) {
    return new FindDefaultNetworkInteract(ethereumNetworkRepositoryType);
  }
}
