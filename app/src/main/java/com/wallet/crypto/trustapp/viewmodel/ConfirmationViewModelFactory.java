package com.wallet.crypto.trustapp.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import com.wallet.crypto.trustapp.interact.SendTransactionInteract;
import com.wallet.crypto.trustapp.router.GasSettingsRouter;

public class ConfirmationViewModelFactory implements ViewModelProvider.Factory {

  private final SendTransactionInteract sendTransactionInteract;
  private GasSettingsRouter gasSettingsRouter;

  public ConfirmationViewModelFactory(SendTransactionInteract sendTransactionInteract,
      GasSettingsRouter gasSettingsRouter) {
    this.sendTransactionInteract = sendTransactionInteract;
    this.gasSettingsRouter = gasSettingsRouter;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new ConfirmationViewModel(sendTransactionInteract, gasSettingsRouter);
  }
}
