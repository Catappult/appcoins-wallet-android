package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.subscriptions.SubscriptionRepository;
import com.asfoundation.wallet.viewmodel.TransactionDetailViewModelFactory;
import dagger.Module;
import dagger.Provides;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@Module public class TransactionDetailModule {

  @Provides TransactionDetailViewModelFactory provideTransactionDetailViewModelFactory(
      FindDefaultNetworkInteract findDefaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      ExternalBrowserRouter externalBrowserRouter, SubscriptionRepository subscriptionRepository) {
    return new TransactionDetailViewModelFactory(findDefaultNetworkInteract,
        findDefaultWalletInteract, externalBrowserRouter, subscriptionRepository, Schedulers.io(),
        AndroidSchedulers.mainThread());
  }

  @Provides ExternalBrowserRouter externalBrowserRouter() {
    return new ExternalBrowserRouter();
  }
}
