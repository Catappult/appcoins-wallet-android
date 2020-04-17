package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.ImportWalletInteract;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.viewmodel.ImportWalletViewModelFactory;
import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;
import javax.inject.Singleton;

@Module(includes = RepositoriesModule.class) class ImportModule {
  @Singleton @Provides ImportWalletInteract provideImportWalletInteract(
      WalletRepositoryType walletRepository, PasswordStore passwordStore,
      PreferencesRepositoryType preferencesRepositoryType) {
    return new ImportWalletInteract(walletRepository, passwordStore, preferencesRepositoryType);
  }

  @Singleton @Provides ImportWalletViewModelFactory provideImportWalletViewModelFactory(
      ImportWalletInteract importWalletInteract, WalletRepositoryType walletRepository) {
    return new ImportWalletViewModelFactory(importWalletInteract, walletRepository,
        new CompositeDisposable());
  }
}
