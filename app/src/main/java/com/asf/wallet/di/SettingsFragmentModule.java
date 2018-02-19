package com.asf.wallet.di;

import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.repository.WalletRepositoryType;
import com.asf.wallet.router.ManageWalletsRouter;
import dagger.Module;
import dagger.Provides;

@Module class SettingsFragmentModule {
  @Provides FindDefaultWalletInteract provideFindDefaultWalletInteract(
      WalletRepositoryType walletRepository) {
    return new FindDefaultWalletInteract(walletRepository);
  }

  @Provides ManageWalletsRouter provideManageWalletsRouter() {
    return new ManageWalletsRouter();
  }
}
