package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.router.TransactionDetailRouter;
import com.asfoundation.wallet.support.SupportRepository;
import io.reactivex.disposables.CompositeDisposable;

public class TransactionDetailViewModelFactory implements ViewModelProvider.Factory {

  private final FindDefaultNetworkInteract findDefaultNetworkInteract;
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final ExternalBrowserRouter externalBrowserRouter;
  private final CompositeDisposable compositeDisposable;
  private final SupportRepository supportRepository;
  private final TransactionDetailRouter transactionDetailRouter;

  public TransactionDetailViewModelFactory(FindDefaultNetworkInteract findDefaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      ExternalBrowserRouter externalBrowserRouter, CompositeDisposable compositeDisposable,
      SupportRepository supportRepository, TransactionDetailRouter transactionDetailRouter) {
    this.findDefaultNetworkInteract = findDefaultNetworkInteract;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.externalBrowserRouter = externalBrowserRouter;
    this.compositeDisposable = compositeDisposable;
    this.supportRepository = supportRepository;
    this.transactionDetailRouter = transactionDetailRouter;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new TransactionDetailViewModel(findDefaultNetworkInteract, findDefaultWalletInteract,
        externalBrowserRouter, compositeDisposable, supportRepository, transactionDetailRouter);
  }
}
