package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.MyTokensRouter;
import com.asfoundation.wallet.viewmodel.AddTokenViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module public class AddTokenModule {

  @Provides AddTokenViewModelFactory addTokenViewModelFactory(AddTokenInteract addTokenInteract,
      FindDefaultWalletInteract findDefaultWalletInteract, MyTokensRouter myTokensRouter) {
    return new AddTokenViewModelFactory(addTokenInteract, findDefaultWalletInteract,
        myTokensRouter);
  }

  @Provides MyTokensRouter provideMyTokensRouter() {
    return new MyTokensRouter();
  }
}
