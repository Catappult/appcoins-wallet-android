package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.BalanceRouter;

public class AddTokenViewModelFactory implements ViewModelProvider.Factory {

  private final AddTokenInteract addTokenInteract;
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final BalanceRouter balanceRouter;

  public AddTokenViewModelFactory(AddTokenInteract addTokenInteract,
      FindDefaultWalletInteract findDefaultWalletInteract, BalanceRouter balanceRouter) {
    this.addTokenInteract = addTokenInteract;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.balanceRouter = balanceRouter;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new AddTokenViewModel(addTokenInteract, findDefaultWalletInteract, balanceRouter);
  }
}
