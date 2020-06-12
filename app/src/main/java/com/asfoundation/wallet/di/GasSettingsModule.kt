package com.asfoundation.wallet.di

import com.asfoundation.wallet.interact.FindDefaultNetworkInteract
import com.asfoundation.wallet.viewmodel.GasSettingsViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class GasSettingsModule {
  @Provides
  fun provideGasSettingsViewModelFactory(findDefaultNetworkInteract: FindDefaultNetworkInteract) =
      GasSettingsViewModelFactory(findDefaultNetworkInteract)
}