package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.BalanceRouter;
import com.asfoundation.wallet.viewmodel.AddTokenViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module public class AddTokenModule {

  @Provides AddTokenViewModelFactory addTokenViewModelFactory(AddTokenInteract addTokenInteract,
      FindDefaultWalletInteract findDefaultWalletInteract, BalanceRouter balanceRouter) {
    return new AddTokenViewModelFactory(addTokenInteract, findDefaultWalletInteract, balanceRouter);
  }

  @Provides BalanceRouter provideMyTokensRouter() {
    return new BalanceRouter();
  }
}
