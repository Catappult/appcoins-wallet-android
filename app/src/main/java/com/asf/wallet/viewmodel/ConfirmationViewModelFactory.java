package com.asf.wallet.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import com.asf.wallet.interact.SendTransactionInteract;
import com.asf.wallet.repository.PendingTransactionService;
import com.asf.wallet.router.GasSettingsRouter;

public class ConfirmationViewModelFactory implements ViewModelProvider.Factory {

  private final SendTransactionInteract sendTransactionInteract;
  private GasSettingsRouter gasSettingsRouter;
  private final PendingTransactionService pendingTransactionService;

  public ConfirmationViewModelFactory(SendTransactionInteract sendTransactionInteract,
      GasSettingsRouter gasSettingsRouter, PendingTransactionService pendingTransactionService) {
    this.sendTransactionInteract = sendTransactionInteract;
    this.gasSettingsRouter = gasSettingsRouter;
    this.pendingTransactionService = pendingTransactionService;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new ConfirmationViewModel(sendTransactionInteract, gasSettingsRouter,
        pendingTransactionService);
  }
}
