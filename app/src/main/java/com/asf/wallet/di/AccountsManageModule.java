package com.asf.wallet.di;

import com.asf.wallet.interact.AddTokenInteract;
import com.asf.wallet.interact.CreateWalletInteract;
import com.asf.wallet.interact.DeleteWalletInteract;
import com.asf.wallet.interact.ExportWalletInteract;
import com.asf.wallet.interact.FetchWalletsInteract;
import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.interact.SetDefaultWalletInteract;
import com.asf.wallet.repository.PasswordStore;
import com.asf.wallet.repository.TokenRepositoryType;
import com.asf.wallet.repository.WalletRepositoryType;
import com.asf.wallet.router.ImportWalletRouter;
import com.asf.wallet.router.TransactionsRouter;
import com.asf.wallet.viewmodel.WalletsViewModelFactory;
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
