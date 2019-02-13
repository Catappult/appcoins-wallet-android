package com.asfoundation.wallet.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.ConfirmationRouter;
import com.asfoundation.wallet.router.TransactionsRouter;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.annotations.NonNull;

public class SendViewModelFactory implements ViewModelProvider.Factory {

  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final FetchGasSettingsInteract fetchGasSettingsInteract;
  private final ConfirmationRouter confirmationRouter;
  private final TransferParser transferParser;
  private final TransactionsRouter transactionsRouter;

  public SendViewModelFactory(FindDefaultWalletInteract findDefaultWalletInteract,
      FetchGasSettingsInteract fetchGasSettingsInteract, ConfirmationRouter confirmationRouter,
      TransferParser transferParser, TransactionsRouter transactionsRouter) {
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.fetchGasSettingsInteract = fetchGasSettingsInteract;
    this.confirmationRouter = confirmationRouter;
    this.transferParser = transferParser;
    this.transactionsRouter = transactionsRouter;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new SendViewModel(findDefaultWalletInteract, fetchGasSettingsInteract,
        confirmationRouter, transferParser, transactionsRouter);
  }
}
