package com.asf.wallet.di;

import com.asf.wallet.interact.ImportWalletInteract;
import com.asf.wallet.repository.PasswordStore;
import com.asf.wallet.repository.WalletRepositoryType;
import com.asf.wallet.viewmodel.ImportWalletViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module class ImportModule {
  @Provides ImportWalletViewModelFactory provideImportWalletViewModelFactory(
      ImportWalletInteract importWalletInteract) {
    return new ImportWalletViewModelFactory(importWalletInteract);
  }

  @Provides ImportWalletInteract provideImportWalletInteract(WalletRepositoryType walletRepository,
      PasswordStore passwordStore) {
    return new ImportWalletInteract(walletRepository, passwordStore);
  }
}
