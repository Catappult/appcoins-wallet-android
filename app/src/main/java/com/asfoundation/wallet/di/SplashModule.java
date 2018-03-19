package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.FetchWalletsInteract;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.viewmodel.SplashViewModelFactory;
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
