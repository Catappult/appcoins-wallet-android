package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;

public class GasSettingsViewModelFactory implements ViewModelProvider.Factory {

  FindDefaultNetworkInteract findDefaultNetworkInteract;

  public GasSettingsViewModelFactory(FindDefaultNetworkInteract findDefaultNetworkInteract) {
    this.findDefaultNetworkInteract = findDefaultNetworkInteract;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new GasSettingsViewModel(findDefaultNetworkInteract);
  }
}
