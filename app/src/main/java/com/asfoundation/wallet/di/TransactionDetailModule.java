package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.ui.MicroRaidenInteractor;
import com.asfoundation.wallet.ui.iab.raiden.Raiden;
import com.asfoundation.wallet.viewmodel.TransactionDetailViewModelFactory;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module public class TransactionDetailModule {

  @Provides TransactionDetailViewModelFactory provideTransactionDetailViewModelFactory(
      FindDefaultNetworkInteract findDefaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      ExternalBrowserRouter externalBrowserRouter, MicroRaidenInteractor microRaidenInteractor) {
    return new TransactionDetailViewModelFactory(findDefaultNetworkInteract,
        findDefaultWalletInteract, externalBrowserRouter, microRaidenInteractor);
  }

  @Provides ExternalBrowserRouter externalBrowserRouter() {
    return new ExternalBrowserRouter();
  }

}
