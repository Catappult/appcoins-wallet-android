package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Delete and fetchTokens wallets
 */
public class DeleteWalletInteract {
  private final WalletRepositoryType walletRepository;
  private final PasswordStore passwordStore;
  private final PreferencesRepositoryType preferencesRepositoryType;

  public DeleteWalletInteract(WalletRepositoryType walletRepository, PasswordStore passwordStore,
      PreferencesRepositoryType preferencesRepositoryType) {
    this.walletRepository = walletRepository;
    this.passwordStore = passwordStore;
    this.preferencesRepositoryType = preferencesRepositoryType;
  }

  public Single<Wallet[]> delete(Wallet wallet) {
    return passwordStore.getPassword(wallet)
        .flatMapCompletable(password -> walletRepository.deleteWallet(wallet.address, password))
        .andThen(Completable.fromAction(
            () -> preferencesRepositoryType.removeWalletValidationStatus(wallet.address)))
        .andThen(Completable.fromAction(
            () -> preferencesRepositoryType.removeWalletImportBackup(wallet.address)))
        .andThen(walletRepository.fetchWallets())
        .observeOn(AndroidSchedulers.mainThread());
  }
}
