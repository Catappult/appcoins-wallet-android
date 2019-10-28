package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.Logger;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import com.asfoundation.wallet.router.GasSettingsRouter;

public class ConfirmationViewModelFactory implements ViewModelProvider.Factory {

  private final SendTransactionInteract sendTransactionInteract;
  private final GasSettingsRouter gasSettingsRouter;
  private final FetchGasSettingsInteract gasSettingsInteract;
  private final Logger logger;

  public ConfirmationViewModelFactory(SendTransactionInteract sendTransactionInteract,
      GasSettingsRouter gasSettingsRouter, FetchGasSettingsInteract gasSettingsInteract,
      Logger logger) {
    this.sendTransactionInteract = sendTransactionInteract;
    this.gasSettingsRouter = gasSettingsRouter;
    this.gasSettingsInteract = gasSettingsInteract;
    this.logger = logger;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new ConfirmationViewModel(sendTransactionInteract, gasSettingsRouter,
        gasSettingsInteract, logger);
  }
}
