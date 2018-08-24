package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.ImportWalletInteract;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.WalletRepository;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.viewmodel.ImportWalletViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module class ImportModule {
  @Provides ImportWalletViewModelFactory provideImportWalletViewModelFactory(
      ImportWalletInteract importWalletInteract, WalletRepository walletRepository) {
    return new ImportWalletViewModelFactory(importWalletInteract);
  }

  @Provides ImportWalletInteract provideImportWalletInteract(WalletRepositoryType walletRepository,
      PasswordStore passwordStore) {
    return new ImportWalletInteract(walletRepository, passwordStore);
  }
}
