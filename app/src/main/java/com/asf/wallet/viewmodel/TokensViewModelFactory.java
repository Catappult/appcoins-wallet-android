package com.asf.wallet.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import com.asf.wallet.interact.FetchTokensInteract;
import com.asf.wallet.router.AddTokenRouter;
import com.asf.wallet.router.ChangeTokenCollectionRouter;
import com.asf.wallet.router.SendRouter;
import com.asf.wallet.router.TransactionsRouter;

public class TokensViewModelFactory implements ViewModelProvider.Factory {

  private final FetchTokensInteract fetchTokensInteract;
  private final AddTokenRouter addTokenRouter;
  private final SendRouter sendRouter;
  private final TransactionsRouter transactionsRouter;
  private final ChangeTokenCollectionRouter changeTokenCollectionRouter;

  public TokensViewModelFactory(FetchTokensInteract fetchTokensInteract,
      AddTokenRouter addTokenRouter, SendRouter sendRouter, TransactionsRouter transactionsRouter,
      ChangeTokenCollectionRouter changeTokenCollectionRouter) {
    this.fetchTokensInteract = fetchTokensInteract;
    this.addTokenRouter = addTokenRouter;
    this.sendRouter = sendRouter;
    this.transactionsRouter = transactionsRouter;
    this.changeTokenCollectionRouter = changeTokenCollectionRouter;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new TokensViewModel(fetchTokensInteract, addTokenRouter, sendRouter,
        transactionsRouter, changeTokenCollectionRouter);
  }
}
