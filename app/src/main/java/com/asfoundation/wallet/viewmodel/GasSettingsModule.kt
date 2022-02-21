package com.asfoundation.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.ui.GasSettingsInteractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@InstallIn(ActivityComponent::class)
@Module
class GasSettingsModule {
  @Provides
  fun provideGasSettingsViewModelFactory(gasSettingsInteractor: GasSettingsInteractor) =
    GasSettingsViewModelFactory(gasSettingsInteractor)
}

class GasSettingsViewModelFactory(private val gasSettingsInteractor: GasSettingsInteractor) :
  ViewModelProvider.Factory {

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return GasSettingsViewModel(gasSettingsInteractor) as T
  }
}