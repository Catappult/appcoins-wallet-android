package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.ImportWalletInteract;
import com.asfoundation.wallet.interact.SetDefaultWalletInteract;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(includes = { RepositoriesModule.class, AccountsManageModule.class }) class ImportModule {
  @Singleton @Provides ImportWalletInteract provideImportWalletInteract(
      WalletRepositoryType walletRepository, PasswordStore passwordStore,
      PreferencesRepositoryType preferencesRepositoryType,
      SetDefaultWalletInteract setDefaultWalletInteract) {
    return new ImportWalletInteract(walletRepository, setDefaultWalletInteract, passwordStore,
        preferencesRepositoryType);
  }
}
