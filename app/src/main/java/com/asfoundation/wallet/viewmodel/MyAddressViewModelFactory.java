package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
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
