package com.asfoundation.wallet.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import com.asfoundation.wallet.router.TransactionsRouter;

public class MyAddressViewModelFactory implements ViewModelProvider.Factory {

  private final TransactionsRouter transactionsRouter;

  public MyAddressViewModelFactory(TransactionsRouter transactionsRouter) {
    this.transactionsRouter = transactionsRouter;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new MyAddressViewModel(transactionsRouter);
  }
}
