package com.asfoundation.wallet.di

import com.asfoundation.wallet.home.usecases.FindNetworkInfoUseCase
import com.asfoundation.wallet.repository.GasPreferenceRepository
import com.asfoundation.wallet.ui.GasSettingsInteractor
import com.asfoundation.wallet.viewmodel.GasSettingsViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class GasSettingsModule {
  @Provides
  fun provideGasSettingsViewModelFactory(gasSettingsInteractor: GasSettingsInteractor) =
      GasSettingsViewModelFactory(gasSettingsInteractor)

  @Provides
  fun providesGasSettingsInteractor(findNetworkInfoUseCase: FindNetworkInfoUseCase,
                                    gasPreferenceRepository: GasPreferenceRepository): GasSettingsInteractor {
    return GasSettingsInteractor(findNetworkInfoUseCase, gasPreferenceRepository)
  }
}