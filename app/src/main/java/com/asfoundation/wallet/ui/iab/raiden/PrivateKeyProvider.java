package com.asfoundation.wallet.ui.iab.raiden;

import android.util.Pair;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.service.AccountKeystoreService;
import com.asfoundation.wallet.util.WalletUtils;
import ethereumj.crypto.ECKey;
import io.reactivex.Single;

public class PrivateKeyProvider {

  private final AccountKeystoreService accountKeystoreService;
  private final PasswordStore passwordStore;
  private Pair<String, ECKey> stringECKeyPair;

  public PrivateKeyProvider(AccountKeystoreService accountKeystoreService,
      PasswordStore passwordStore) {
    this.accountKeystoreService = accountKeystoreService;
    this.passwordStore = passwordStore;
  }

  public Single<ECKey> get(String walletAddress) {
    if (stringECKeyPair != null && stringECKeyPair.first.equalsIgnoreCase(walletAddress)) {
      return Single.just(stringECKeyPair.second);
    }
    return Single.just(new Wallet(walletAddress))
        .flatMap(wallet -> passwordStore.getPassword(wallet)
            .flatMap(password -> accountKeystoreService.exportAccount(wallet, password, password)
                .map(json -> ECKey.fromPrivate(WalletUtils.loadCredentials(password, json)
                    .getEcKeyPair()
                    .getPrivateKey()))))
        .doOnSuccess(ecKey -> stringECKeyPair = new Pair<>(walletAddress, ecKey));
  }
}
