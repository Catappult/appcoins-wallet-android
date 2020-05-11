package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.CreateWalletInteract;
import com.asfoundation.wallet.interact.DeleteWalletInteract;
import com.asfoundation.wallet.interact.ExportWalletInteract;
import com.asfoundation.wallet.interact.FetchWalletsInteract;
import com.asfoundation.wallet.interact.SetDefaultWalletInteract;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.repository.SharedPreferencesRepository;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.ui.balance.BalanceInteract;
import com.asfoundation.wallet.ui.wallets.WalletDetailsInteractor;
import com.asfoundation.wallet.ui.wallets.WalletsInteract;
import dagger.Module;
import dagger.Provides;

@Module class AccountsManageModule {

  @Provides SetDefaultWalletInteract provideSetDefaultAccountInteract(
      WalletRepositoryType accountRepository) {
    return new SetDefaultWalletInteract(accountRepository);
  }

  @Provides DeleteWalletInteract provideDeleteAccountInteract(
      WalletRepositoryType accountRepository, PasswordStore store,
      PreferencesRepositoryType preferencesRepositoryType) {
    return new DeleteWalletInteract(accountRepository, store, preferencesRepositoryType);
  }

  @Provides FetchWalletsInteract provideFetchAccountsInteract(
      WalletRepositoryType accountRepository) {
    return new FetchWalletsInteract(accountRepository);
  }

  @Provides ExportWalletInteract provideExportWalletInteract(WalletRepositoryType walletRepository,
      PasswordStore passwordStore) {
    return new ExportWalletInteract(walletRepository, passwordStore);
  }

  @Provides WalletsInteract provideWalletsInteract(BalanceInteract balanceInteract,
      FetchWalletsInteract fetchWalletsInteract, CreateWalletInteract createWalletInteract,
      SharedPreferencesRepository sharedPreferencesRepository) {
    return new WalletsInteract(balanceInteract, fetchWalletsInteract, createWalletInteract,
        sharedPreferencesRepository);
  }

  @Provides WalletDetailsInteractor provideWalletDetailInteract(BalanceInteract balanceInteract,
      SetDefaultWalletInteract setDefaultWalletInteract) {
    return new WalletDetailsInteractor(balanceInteract, setDefaultWalletInteract);
  }
}
