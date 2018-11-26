package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.viewmodel.TransactionDetailViewModelFactory;
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

  @Provides ExternalBrowserRouter externalBrowserRouter() {
    return new ExternalBrowserRouter();
  }

}
