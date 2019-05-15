package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.interact.CreateWalletInteract;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.DeleteWalletInteract;
import com.asfoundation.wallet.interact.ExportWalletInteract;
import com.asfoundation.wallet.interact.FetchWalletsInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.SetDefaultWalletInteract;
import com.asfoundation.wallet.router.ImportWalletRouter;
import com.asfoundation.wallet.router.TransactionsRouter;
import com.asfoundation.wallet.service.AccountWalletService;
import javax.inject.Inject;

public class WalletsViewModelFactory implements ViewModelProvider.Factory {

  private final CreateWalletInteract createWalletInteract;
  private final SetDefaultWalletInteract setDefaultWalletInteract;
  private final DeleteWalletInteract deleteWalletInteract;
  private final FetchWalletsInteract fetchWalletsInteract;
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final ExportWalletInteract exportWalletInteract;

  private final ImportWalletRouter importWalletRouter;
  private final TransactionsRouter transactionsRouter;
  private final AddTokenInteract addTokenInteract;
  private final DefaultTokenProvider defaultTokenProvider;

  @Inject public WalletsViewModelFactory(CreateWalletInteract createWalletInteract,
      SetDefaultWalletInteract setDefaultWalletInteract, DeleteWalletInteract deleteWalletInteract,
      FetchWalletsInteract fetchWalletsInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      ExportWalletInteract exportWalletInteract, ImportWalletRouter importWalletRouter,
      TransactionsRouter transactionsRouter, AddTokenInteract addTokenInteract,
      DefaultTokenProvider defaultTokenProvider) {
    this.createWalletInteract = createWalletInteract;
    this.setDefaultWalletInteract = setDefaultWalletInteract;
    this.deleteWalletInteract = deleteWalletInteract;
    this.fetchWalletsInteract = fetchWalletsInteract;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.exportWalletInteract = exportWalletInteract;
    this.importWalletRouter = importWalletRouter;
    this.transactionsRouter = transactionsRouter;
    this.addTokenInteract = addTokenInteract;
    this.defaultTokenProvider = defaultTokenProvider;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new WalletsViewModel(createWalletInteract, setDefaultWalletInteract,
        deleteWalletInteract, fetchWalletsInteract, findDefaultWalletInteract, exportWalletInteract,
        importWalletRouter, transactionsRouter, addTokenInteract, defaultTokenProvider);
  }
}
