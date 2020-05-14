package com.asfoundation.wallet.di;

import com.asfoundation.wallet.backup.FileInteractor;
import com.asfoundation.wallet.interact.ImportWalletInteract;
import com.asfoundation.wallet.interact.SetDefaultWalletInteract;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.ui.balance.BalanceInteract;
import com.asfoundation.wallet.ui.balance.ImportWalletPasswordInteractor;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(includes = { RepositoriesModule.class, AccountsManageModule.class }) class ImportModule {
  @Singleton @Provides ImportWalletInteract provideImportWalletInteract(
      WalletRepositoryType walletRepository, PasswordStore passwordStore,
      PreferencesRepositoryType preferencesRepositoryType,
      SetDefaultWalletInteract setDefaultWalletInteract, FileInteractor fileInteractor) {
    return new ImportWalletInteract(walletRepository, setDefaultWalletInteract, passwordStore,
        preferencesRepositoryType, fileInteractor);
  }

  @Singleton @Provides ImportWalletPasswordInteractor provideImportWalletInteractor(Gson gson,
      BalanceInteract balanceInteract, ImportWalletInteract importWalletInteract) {
    return new ImportWalletPasswordInteractor(gson, balanceInteract, importWalletInteract);
  }
}
