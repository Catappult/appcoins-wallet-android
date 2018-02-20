package com.asf.wallet.di;

import com.asf.wallet.interact.FetchWalletsInteract;
import com.asf.wallet.repository.WalletRepositoryType;
import com.asf.wallet.viewmodel.SplashViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module public class SplashModule {

  @Provides SplashViewModelFactory provideSplashViewModelFactory(
      FetchWalletsInteract fetchWalletsInteract) {
    return new SplashViewModelFactory(fetchWalletsInteract);
  }

  @Provides FetchWalletsInteract provideFetchWalletInteract(WalletRepositoryType walletRepository) {
    return new FetchWalletsInteract(walletRepository);
  }
}
