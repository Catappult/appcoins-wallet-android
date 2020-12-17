package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.logging.Logger;
import com.asfoundation.wallet.router.GasSettingsRouter;
import com.asfoundation.wallet.ui.ConfirmationInteractor;

public class ConfirmationViewModelFactory implements ViewModelProvider.Factory {

  private final ConfirmationInteractor confirmationInteractor;
  private final GasSettingsRouter gasSettingsRouter;
  private final Logger logger;

  public ConfirmationViewModelFactory(ConfirmationInteractor confirmationInteractor,
      GasSettingsRouter gasSettingsRouter, Logger logger) {
    this.confirmationInteractor = confirmationInteractor;
    this.gasSettingsRouter = gasSettingsRouter;
    this.logger = logger;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new ConfirmationViewModel(confirmationInteractor, gasSettingsRouter, logger);
  }
}
