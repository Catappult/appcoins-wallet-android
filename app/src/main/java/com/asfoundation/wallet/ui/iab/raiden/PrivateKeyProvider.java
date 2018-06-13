package com.asfoundation.wallet.ui.iab.raiden;

import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.service.AccountKeystoreService;
import ethereumj.crypto.ECKey;
import org.web3j.crypto.WalletUtils;

public class PrivateKeyProvider {

  private final WalletRepositoryType walletRepositoryType;
  private final AccountKeystoreService accountKeystoreService;
  private final PasswordStore passwordStore;

  public PrivateKeyProvider(WalletRepositoryType walletRepositoryType,
      AccountKeystoreService accountKeystoreService, PasswordStore passwordStore) {
    this.walletRepositoryType = walletRepositoryType;
    this.accountKeystoreService = accountKeystoreService;
    this.passwordStore = passwordStore;
  }

  public ECKey get(String walletAddress) {
    return walletRepositoryType.findWallet(walletAddress)
        .flatMap(wallet -> passwordStore.getPassword(wallet)
            .flatMap(password -> accountKeystoreService.exportAccount(wallet, password, password)
                .map(json -> ECKey.fromPrivate(WalletUtils.loadCredentials(password, json)
                    .getEcKeyPair()
                    .getPrivateKey()))))
        .blockingGet();
  }
}
