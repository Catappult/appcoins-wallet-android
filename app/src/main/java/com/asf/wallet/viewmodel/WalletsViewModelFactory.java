package com.asf.wallet.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import com.asf.wallet.interact.AddTokenInteract;
import com.asf.wallet.interact.CreateWalletInteract;
import com.asf.wallet.interact.DeleteWalletInteract;
import com.asf.wallet.interact.ExportWalletInteract;
import com.asf.wallet.interact.FetchWalletsInteract;
import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.interact.SetDefaultWalletInteract;
import com.asf.wallet.router.ImportWalletRouter;
import com.asf.wallet.router.TransactionsRouter;
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

  @Inject public WalletsViewModelFactory(CreateWalletInteract createWalletInteract,
      SetDefaultWalletInteract setDefaultWalletInteract, DeleteWalletInteract deleteWalletInteract,
      FetchWalletsInteract fetchWalletsInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      ExportWalletInteract exportWalletInteract, ImportWalletRouter importWalletRouter,
      TransactionsRouter transactionsRouter, AddTokenInteract addTokenInteract) {
    this.createWalletInteract = createWalletInteract;
    this.setDefaultWalletInteract = setDefaultWalletInteract;
    this.deleteWalletInteract = deleteWalletInteract;
    this.fetchWalletsInteract = fetchWalletsInteract;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.exportWalletInteract = exportWalletInteract;
    this.importWalletRouter = importWalletRouter;
    this.transactionsRouter = transactionsRouter;
    this.addTokenInteract = addTokenInteract;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new WalletsViewModel(createWalletInteract, setDefaultWalletInteract,
        deleteWalletInteract, fetchWalletsInteract, findDefaultWalletInteract, exportWalletInteract,
        importWalletRouter, transactionsRouter, addTokenInteract);
  }
}
