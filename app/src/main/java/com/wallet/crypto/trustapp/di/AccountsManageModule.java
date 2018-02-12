package com.wallet.crypto.trustapp.di;

import com.wallet.crypto.trustapp.interact.AddTokenInteract;
import com.wallet.crypto.trustapp.interact.CreateWalletInteract;
import com.wallet.crypto.trustapp.interact.DeleteWalletInteract;
import com.wallet.crypto.trustapp.interact.ExportWalletInteract;
import com.wallet.crypto.trustapp.interact.FetchWalletsInteract;
import com.wallet.crypto.trustapp.interact.FindDefaultWalletInteract;
import com.wallet.crypto.trustapp.interact.SetDefaultWalletInteract;
import com.wallet.crypto.trustapp.repository.PasswordStore;
import com.wallet.crypto.trustapp.repository.TokenRepositoryType;
import com.wallet.crypto.trustapp.repository.WalletRepositoryType;
import com.wallet.crypto.trustapp.router.ImportWalletRouter;
import com.wallet.crypto.trustapp.router.TransactionsRouter;
import com.wallet.crypto.trustapp.viewmodel.WalletsViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module class AccountsManageModule {

  @Provides AddTokenInteract provideAddTokenInteract(TokenRepositoryType tokenRepository,
      WalletRepositoryType walletRepository) {
    return new AddTokenInteract(walletRepository, tokenRepository);
  }

  @Provides WalletsViewModelFactory provideAccountsManageViewModelFactory(
      CreateWalletInteract createWalletInteract, SetDefaultWalletInteract setDefaultWalletInteract,
      DeleteWalletInteract deleteWalletInteract, FetchWalletsInteract fetchWalletsInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      ExportWalletInteract exportWalletInteract, ImportWalletRouter importWalletRouter,
      TransactionsRouter transactionsRouter, AddTokenInteract addTokenInteract) {
    return new WalletsViewModelFactory(createWalletInteract, setDefaultWalletInteract,
        deleteWalletInteract, fetchWalletsInteract, findDefaultWalletInteract, exportWalletInteract,
        importWalletRouter, transactionsRouter, addTokenInteract);
  }

  @Provides CreateWalletInteract provideCreateAccountInteract(
      WalletRepositoryType accountRepository, PasswordStore passwordStore) {
    return new CreateWalletInteract(accountRepository, passwordStore);
  }

  @Provides SetDefaultWalletInteract provideSetDefaultAccountInteract(
      WalletRepositoryType accountRepository) {
    return new SetDefaultWalletInteract(accountRepository);
  }

  @Provides DeleteWalletInteract provideDeleteAccountInteract(
      WalletRepositoryType accountRepository, PasswordStore store) {
    return new DeleteWalletInteract(accountRepository, store);
  }

  @Provides FetchWalletsInteract provideFetchAccountsInteract(
      WalletRepositoryType accountRepository) {
    return new FetchWalletsInteract(accountRepository);
  }

  @Provides FindDefaultWalletInteract provideFindDefaultAccountInteract(
      WalletRepositoryType accountRepository) {
    return new FindDefaultWalletInteract(accountRepository);
  }

  @Provides ExportWalletInteract provideExportWalletInteract(WalletRepositoryType walletRepository,
      PasswordStore passwordStore) {
    return new ExportWalletInteract(walletRepository, passwordStore);
  }

  @Provides ImportWalletRouter provideImportAccountRouter() {
    return new ImportWalletRouter();
  }

  @Provides TransactionsRouter provideTransactionsRouter() {
    return new TransactionsRouter();
  }
}
