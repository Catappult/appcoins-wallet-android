package com.asfoundation.wallet.ui.balance.detail;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.appcoins.wallet.bdsbilling.WalletService;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.router.TransactionDetailRouter;
import com.asfoundation.wallet.support.SupportInteractor;
import io.reactivex.disposables.CompositeDisposable;

public class TransactionDetailViewModelFactory implements ViewModelProvider.Factory {

  private final TransactionDetailData data;
  private final TransactionDetailInteractor interactor;
  private final FindDefaultNetworkInteract findDefaultNetworkInteract;
  private final WalletService walletService;
  private final ExternalBrowserRouter externalBrowserRouter;
  private final CompositeDisposable compositeDisposable;
  private final SupportInteractor supportInteractor;
  private final TransactionDetailRouter transactionDetailRouter;

  public TransactionDetailViewModelFactory(TransactionDetailData data,
      TransactionDetailInteractor interactor, FindDefaultNetworkInteract findDefaultNetworkInteract,
      WalletService walletService, ExternalBrowserRouter externalBrowserRouter,
      CompositeDisposable compositeDisposable, SupportInteractor supportInteractor,
      TransactionDetailRouter transactionDetailRouter) {
    this.data = data;
    this.interactor = interactor;
    this.findDefaultNetworkInteract = findDefaultNetworkInteract;
    this.walletService = walletService;
    this.externalBrowserRouter = externalBrowserRouter;
    this.compositeDisposable = compositeDisposable;
    this.supportInteractor = supportInteractor;
    this.transactionDetailRouter = transactionDetailRouter;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new TransactionDetailViewModel(data, interactor, findDefaultNetworkInteract,
        walletService, externalBrowserRouter, compositeDisposable, supportInteractor,
        transactionDetailRouter);
  }
}
