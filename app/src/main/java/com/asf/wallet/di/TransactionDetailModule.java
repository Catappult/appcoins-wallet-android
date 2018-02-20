package com.asf.wallet.di;

import com.asf.wallet.interact.FindDefaultNetworkInteract;
import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.repository.EthereumNetworkRepositoryType;
import com.asf.wallet.repository.WalletRepositoryType;
import com.asf.wallet.router.ExternalBrowserRouter;
import com.asf.wallet.viewmodel.TransactionDetailViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module public class TransactionDetailModule {

  @Provides TransactionDetailViewModelFactory provideTransactionDetailViewModelFactory(
      FindDefaultNetworkInteract findDefaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      ExternalBrowserRouter externalBrowserRouter) {
    return new TransactionDetailViewModelFactory(findDefaultNetworkInteract,
        findDefaultWalletInteract, externalBrowserRouter);
  }

  @Provides FindDefaultNetworkInteract provideFindDefaultNetworkInteract(
      EthereumNetworkRepositoryType ethereumNetworkRepository) {
    return new FindDefaultNetworkInteract(ethereumNetworkRepository);
  }

  @Provides ExternalBrowserRouter externalBrowserRouter() {
    return new ExternalBrowserRouter();
  }

  @Provides FindDefaultWalletInteract findDefaultWalletInteract(
      WalletRepositoryType walletRepository) {
    return new FindDefaultWalletInteract(walletRepository);
  }
}
