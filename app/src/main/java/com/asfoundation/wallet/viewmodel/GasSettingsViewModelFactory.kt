package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.ui.GasSettingsInteractor;

public class GasSettingsViewModelFactory implements ViewModelProvider.Factory {

  private GasSettingsInteractor gasSettingsInteractor;

  public GasSettingsViewModelFactory(GasSettingsInteractor gasSettingsInteractor) {
    this.gasSettingsInteractor = gasSettingsInteractor;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new GasSettingsViewModel(gasSettingsInteractor);
  }
}
