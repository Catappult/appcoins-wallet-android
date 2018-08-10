package com.asfoundation.wallet.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.ui.MicroRaidenInteractor;

public class TransactionDetailViewModelFactory implements ViewModelProvider.Factory {

  private final FindDefaultNetworkInteract findDefaultNetworkInteract;
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final ExternalBrowserRouter externalBrowserRouter;
  private final MicroRaidenInteractor microRaidenInteractor;

  public TransactionDetailViewModelFactory(FindDefaultNetworkInteract findDefaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      ExternalBrowserRouter externalBrowserRouter, MicroRaidenInteractor microRaidenInteractor) {
    this.findDefaultNetworkInteract = findDefaultNetworkInteract;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.externalBrowserRouter = externalBrowserRouter;
    this.microRaidenInteractor = microRaidenInteractor;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new TransactionDetailViewModel(findDefaultNetworkInteract, findDefaultWalletInteract,
        externalBrowserRouter, microRaidenInteractor);
  }
}
