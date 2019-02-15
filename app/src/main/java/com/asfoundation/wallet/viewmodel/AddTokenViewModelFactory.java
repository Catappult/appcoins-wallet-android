package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.MyTokensRouter;

public class AddTokenViewModelFactory implements ViewModelProvider.Factory {

  private final AddTokenInteract addTokenInteract;
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final MyTokensRouter myTokensRouter;

  public AddTokenViewModelFactory(AddTokenInteract addTokenInteract,
      FindDefaultWalletInteract findDefaultWalletInteract, MyTokensRouter myTokensRouter) {
    this.addTokenInteract = addTokenInteract;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.myTokensRouter = myTokensRouter;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new AddTokenViewModel(addTokenInteract, findDefaultWalletInteract, myTokensRouter);
  }
}
