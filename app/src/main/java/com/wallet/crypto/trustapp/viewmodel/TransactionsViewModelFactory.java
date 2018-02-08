package com.wallet.crypto.trustapp.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.wallet.crypto.trustapp.interact.FetchTokensInteract;
import com.wallet.crypto.trustapp.interact.FetchTransactionsInteract;
import com.wallet.crypto.trustapp.interact.FindDefaultNetworkInteract;
import com.wallet.crypto.trustapp.interact.FindDefaultWalletInteract;
import com.wallet.crypto.trustapp.router.ExternalBrowserRouter;
import com.wallet.crypto.trustapp.router.ManageWalletsRouter;
import com.wallet.crypto.trustapp.router.MyAddressRouter;
import com.wallet.crypto.trustapp.router.MyTokensRouter;
import com.wallet.crypto.trustapp.router.SendRouter;
import com.wallet.crypto.trustapp.router.SettingsRouter;
import com.wallet.crypto.trustapp.router.TransactionDetailRouter;

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

    public TransactionsViewModelFactory(
            FindDefaultNetworkInteract findDefaultNetworkInteract,
            FindDefaultWalletInteract findDefaultWalletInteract,
            FetchTransactionsInteract fetchTransactionsInteract,
            ManageWalletsRouter manageWalletsRouter,
            SettingsRouter settingsRouter,
            SendRouter sendRouter,
            TransactionDetailRouter transactionDetailRouter,
            MyAddressRouter myAddressRouter,
            MyTokensRouter myTokensRouter,
            ExternalBrowserRouter externalBrowserRouter, FetchTokensInteract fetchTokensInteract) {
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

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new TransactionsViewModel(
                findDefaultNetworkInteract,
                findDefaultWalletInteract,
                fetchTransactionsInteract,
                manageWalletsRouter,
                settingsRouter,
                sendRouter,
                transactionDetailRouter,
                myAddressRouter,
                myTokensRouter,
                externalBrowserRouter, fetchTokensInteract);
    }
}
