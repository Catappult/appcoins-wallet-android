package com.asfoundation.wallet.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.appcoins.wallet.feature.walletInfo.data.wallet.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.router.TransferConfirmationRouter;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.annotations.NonNull;

public class SendViewModelFactory implements ViewModelProvider.Factory {

  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final FetchGasSettingsInteract fetchGasSettingsInteract;
  private final TransferConfirmationRouter transferConfirmationRouter;
  private final TransferParser transferParser;

  public SendViewModelFactory(FindDefaultWalletInteract findDefaultWalletInteract,
      FetchGasSettingsInteract fetchGasSettingsInteract,
      TransferConfirmationRouter transferConfirmationRouter, TransferParser transferParser) {
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.fetchGasSettingsInteract = fetchGasSettingsInteract;
    this.transferConfirmationRouter = transferConfirmationRouter;
    this.transferParser = transferParser;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new SendViewModel(findDefaultWalletInteract, fetchGasSettingsInteract,
        transferConfirmationRouter, transferParser);
  }
}