package com.asf.wallet.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import com.asf.wallet.interact.FetchGasSettingsInteract;
import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.router.ConfirmationRouter;
import com.asf.wallet.util.TransferParser;
import io.reactivex.annotations.NonNull;

public class SendViewModelFactory implements ViewModelProvider.Factory {

  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final FetchGasSettingsInteract fetchGasSettingsInteract;
  private final ConfirmationRouter confirmationRouter;
  private final TransferParser transferParser;

  public SendViewModelFactory(FindDefaultWalletInteract findDefaultWalletInteract,
      FetchGasSettingsInteract fetchGasSettingsInteract, ConfirmationRouter confirmationRouter,
      TransferParser transferParser) {
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.fetchGasSettingsInteract = fetchGasSettingsInteract;
    this.confirmationRouter = confirmationRouter;
    this.transferParser = transferParser;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new SendViewModel(findDefaultWalletInteract, fetchGasSettingsInteract,
        confirmationRouter, transferParser);
  }
}
