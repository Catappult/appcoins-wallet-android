package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase;
import com.asfoundation.wallet.home.usecases.FindDefaultWalletUseCase;
import com.asfoundation.wallet.home.usecases.FindNetworkInfoUseCase;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.router.TransactionDetailRouter;
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService;
import io.reactivex.disposables.CompositeDisposable;

public class TransactionDetailViewModelFactory implements ViewModelProvider.Factory {

  private final FindDefaultWalletUseCase findDefaultWalletUseCase;
  private final FindNetworkInfoUseCase findNetworkInfoUseCase;
  private final ExternalBrowserRouter externalBrowserRouter;
  private final CompositeDisposable compositeDisposable;
  private final DisplayChatUseCase displayChatUseCase;
  private final TransactionDetailRouter transactionDetailRouter;
  private final LocalCurrencyConversionService localCurrencyConversionService;

  public TransactionDetailViewModelFactory(FindDefaultWalletUseCase findDefaultWalletUseCase,
      FindNetworkInfoUseCase findNetworkInfoUseCase, ExternalBrowserRouter externalBrowserRouter,
      CompositeDisposable compositeDisposable, DisplayChatUseCase displayChatUseCase,
      TransactionDetailRouter transactionDetailRouter,
      LocalCurrencyConversionService localCurrencyConversionService) {
    this.findDefaultWalletUseCase = findDefaultWalletUseCase;
    this.findNetworkInfoUseCase = findNetworkInfoUseCase;
    this.externalBrowserRouter = externalBrowserRouter;
    this.compositeDisposable = compositeDisposable;
    this.displayChatUseCase = displayChatUseCase;
    this.transactionDetailRouter = transactionDetailRouter;
    this.localCurrencyConversionService = localCurrencyConversionService;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new TransactionDetailViewModel(findDefaultWalletUseCase, findNetworkInfoUseCase,
        externalBrowserRouter, compositeDisposable, displayChatUseCase, transactionDetailRouter,
        localCurrencyConversionService);
  }
}
