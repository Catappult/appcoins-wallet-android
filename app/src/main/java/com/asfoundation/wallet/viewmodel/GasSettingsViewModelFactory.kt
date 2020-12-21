package com.asfoundation.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.ui.GasSettingsInteractor

class GasSettingsViewModelFactory(private val gasSettingsInteractor: GasSettingsInteractor) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return GasSettingsViewModel(gasSettingsInteractor) as T
  }
}