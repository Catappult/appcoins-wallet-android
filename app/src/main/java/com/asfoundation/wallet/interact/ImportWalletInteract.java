package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.rx.operator.Operators;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ImportWalletInteract {

  private final WalletRepositoryType walletRepository;
  private final PasswordStore passwordStore;
  private final PreferencesRepositoryType preferencesRepositoryType;

  public ImportWalletInteract(WalletRepositoryType walletRepository, PasswordStore passwordStore,
      PreferencesRepositoryType preferencesRepositoryType) {
    this.walletRepository = walletRepository;
    this.passwordStore = passwordStore;
    this.preferencesRepositoryType = preferencesRepositoryType;
  }

  public Single<Wallet> importKeystore(String keystore, String password) {
    return passwordStore.generatePassword()
        .flatMap(
            newPassword -> walletRepository.importKeystoreToWallet(keystore, password, newPassword)
                .compose(Operators.savePassword(passwordStore, walletRepository, newPassword)))
        .doOnSuccess(
            wallet -> preferencesRepositoryType.removeWalletValidationStatus(wallet.address))
        .observeOn(AndroidSchedulers.mainThread());
  }

  public Single<Wallet> importPrivateKey(String privateKey) {
    return passwordStore.generatePassword()
        .flatMap(newPassword -> walletRepository.importPrivateKeyToWallet(privateKey, newPassword)
            .compose(Operators.savePassword(passwordStore, walletRepository, newPassword)))
        .doOnSuccess(
            wallet -> preferencesRepositoryType.removeWalletValidationStatus(wallet.address))
        .observeOn(AndroidSchedulers.mainThread());
  }
}
