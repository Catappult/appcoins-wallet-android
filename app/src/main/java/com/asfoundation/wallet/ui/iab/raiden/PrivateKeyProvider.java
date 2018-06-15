package com.asfoundation.wallet.ui.iab.raiden;

import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.service.AccountKeystoreService;
import com.asfoundation.wallet.util.WalletUtils;
import ethereumj.crypto.ECKey;
import io.reactivex.Single;

public class PrivateKeyProvider {

  private final AccountKeystoreService accountKeystoreService;
  private final PasswordStore passwordStore;

  public PrivateKeyProvider(
      AccountKeystoreService accountKeystoreService, PasswordStore passwordStore) {
    this.accountKeystoreService = accountKeystoreService;
    this.passwordStore = passwordStore;
  }

  public Single<ECKey> get(String walletAddress) {
    return Single.just(new Wallet(walletAddress))
        .flatMap(wallet -> passwordStore.getPassword(wallet)
            .flatMap(password -> accountKeystoreService.exportAccount(wallet, password, password)
                .map(json -> ECKey.fromPrivate(WalletUtils.loadCredentials(password, json)
                    .getEcKeyPair()
                    .getPrivateKey()))));
  }
}
