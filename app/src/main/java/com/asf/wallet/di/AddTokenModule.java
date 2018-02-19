package com.asf.wallet.di;

import com.asf.wallet.interact.AddTokenInteract;
import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.repository.TokenRepositoryType;
import com.asf.wallet.repository.WalletRepositoryType;
import com.asf.wallet.router.MyTokensRouter;
import com.asf.wallet.viewmodel.AddTokenViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module public class AddTokenModule {

  @Provides AddTokenViewModelFactory addTokenViewModelFactory(AddTokenInteract addTokenInteract,
      FindDefaultWalletInteract findDefaultWalletInteract, MyTokensRouter myTokensRouter) {
    return new AddTokenViewModelFactory(addTokenInteract, findDefaultWalletInteract,
        myTokensRouter);
  }

  @Provides AddTokenInteract provideAddTokenInteract(TokenRepositoryType tokenRepository,
      WalletRepositoryType walletRepository) {
    return new AddTokenInteract(walletRepository, tokenRepository);
  }

  @Provides FindDefaultWalletInteract provideFindDefaultWalletInteract(
      WalletRepositoryType walletRepository) {
    return new FindDefaultWalletInteract(walletRepository);
  }

  @Provides MyTokensRouter provideMyTokensRouter() {
    return new MyTokensRouter();
  }
}
