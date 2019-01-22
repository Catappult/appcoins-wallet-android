package com.asfoundation.wallet.di;

import com.asfoundation.wallet.router.TransactionsRouter;
import com.asfoundation.wallet.viewmodel.MyAddressViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module public class MyAddressModule {

  @Provides MyAddressViewModelFactory providesMyAddressViewModelFactory(
      TransactionsRouter transactionsRouter) {
    return new MyAddressViewModelFactory(transactionsRouter);
  }

  @Provides TransactionsRouter provideTransactionsRouter() {
    return new TransactionsRouter();
  }
}
