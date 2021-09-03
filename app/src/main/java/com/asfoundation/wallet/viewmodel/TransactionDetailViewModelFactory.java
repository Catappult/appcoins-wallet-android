package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.router.TransactionDetailRouter;
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService;
import com.asfoundation.wallet.support.SupportInteractor;
import io.reactivex.disposables.CompositeDisposable;

public class TransactionDetailViewModelFactory implements ViewModelProvider.Factory {

  private final FindDefaultNetworkInteract findDefaultNetworkInteract;
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final ExternalBrowserRouter externalBrowserRouter;
  private final CompositeDisposable compositeDisposable;
  private final SupportInteractor supportInteractor;
  private final TransactionDetailRouter transactionDetailRouter;
  private final LocalCurrencyConversionService localCurrencyConversionService;

  public TransactionDetailViewModelFactory(FindDefaultNetworkInteract findDefaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      ExternalBrowserRouter externalBrowserRouter, CompositeDisposable compositeDisposable,
      SupportInteractor supportInteractor, TransactionDetailRouter transactionDetailRouter,
      LocalCurrencyConversionService localCurrencyConversionService) {
    this.findDefaultNetworkInteract = findDefaultNetworkInteract;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.externalBrowserRouter = externalBrowserRouter;
    this.compositeDisposable = compositeDisposable;
    this.supportInteractor = supportInteractor;
    this.transactionDetailRouter = transactionDetailRouter;
    this.localCurrencyConversionService = localCurrencyConversionService;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new TransactionDetailViewModel(findDefaultNetworkInteract, findDefaultWalletInteract,
        externalBrowserRouter, compositeDisposable, supportInteractor, transactionDetailRouter,
        localCurrencyConversionService);
  }
}
