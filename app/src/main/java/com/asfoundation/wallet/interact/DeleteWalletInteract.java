package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import io.reactivex.Completable;

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

  public Completable delete(String address) {
    return passwordStore.getPassword(address)
        .flatMapCompletable(password -> walletRepository.deleteWallet(address, password))
        .andThen(preferencesRepositoryType.removeWalletValidationStatus(address))
        .andThen(preferencesRepositoryType.removeWalletImportBackup(address))
        .andThen(preferencesRepositoryType.removeBackupNotificationSeenTime(address));
  }
}
