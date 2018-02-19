package com.asf.wallet.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import com.asf.wallet.interact.FetchTokensInteract;
import com.asf.wallet.interact.FetchTransactionsInteract;
import com.asf.wallet.interact.FindDefaultNetworkInteract;
import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.router.ExternalBrowserRouter;
import com.asf.wallet.router.ManageWalletsRouter;
import com.asf.wallet.router.MyAddressRouter;
import com.asf.wallet.router.MyTokensRouter;
import com.asf.wallet.router.SendRouter;
import com.asf.wallet.router.SettingsRouter;
import com.asf.wallet.router.TransactionDetailRouter;

public class TransactionsViewModelFactory implements ViewModelProvider.Factory {

  private final FindDefaultNetworkInteract findDefaultNetworkInteract;
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final FetchTransactionsInteract fetchTransactionsInteract;
  private final ManageWalletsRouter manageWalletsRouter;
  private final SettingsRouter settingsRouter;
  private final SendRouter sendRouter;
  private final TransactionDetailRouter transactionDetailRouter;
  private final MyAddressRouter myAddressRouter;
  private final MyTokensRouter myTokensRouter;
  private final ExternalBrowserRouter externalBrowserRouter;
  private final FetchTokensInteract fetchTokensInteract;

  public TransactionsViewModelFactory(FindDefaultNetworkInteract findDefaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      FetchTransactionsInteract fetchTransactionsInteract, ManageWalletsRouter manageWalletsRouter,
      SettingsRouter settingsRouter, SendRouter sendRouter,
      TransactionDetailRouter transactionDetailRouter, MyAddressRouter myAddressRouter,
      MyTokensRouter myTokensRouter, ExternalBrowserRouter externalBrowserRouter,
      FetchTokensInteract fetchTokensInteract) {
    this.findDefaultNetworkInteract = findDefaultNetworkInteract;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.fetchTransactionsInteract = fetchTransactionsInteract;
    this.manageWalletsRouter = manageWalletsRouter;
    this.settingsRouter = settingsRouter;
    this.sendRouter = sendRouter;
    this.transactionDetailRouter = transactionDetailRouter;
    this.myAddressRouter = myAddressRouter;
    this.myTokensRouter = myTokensRouter;
    this.externalBrowserRouter = externalBrowserRouter;
    this.fetchTokensInteract = fetchTokensInteract;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new TransactionsViewModel(findDefaultNetworkInteract, findDefaultWalletInteract,
        fetchTransactionsInteract, manageWalletsRouter, settingsRouter, sendRouter,
        transactionDetailRouter, myAddressRouter, myTokensRouter, externalBrowserRouter,
        fetchTokensInteract);
  }
}
