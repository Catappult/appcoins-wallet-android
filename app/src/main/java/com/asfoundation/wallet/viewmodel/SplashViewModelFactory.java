package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.interact.FetchWalletsInteract;

public class SplashViewModelFactory implements ViewModelProvider.Factory {

  private final FetchWalletsInteract fetchWalletsInteract;

  public SplashViewModelFactory(FetchWalletsInteract fetchWalletsInteract) {
    this.fetchWalletsInteract = fetchWalletsInteract;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new SplashViewModel(fetchWalletsInteract);
  }
}
