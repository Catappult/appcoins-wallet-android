package com.appcoins.wallet.feature.walletInfo.data.authentication.rxOperator;

import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore;
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet;
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType;
import io.reactivex.CompletableOperator;
import io.reactivex.SingleTransformer;

public class Operators {

  public static SingleTransformer<Wallet, Wallet> savePassword(PasswordStore passwordStore,
      WalletRepositoryType walletRepository, String password) {
    return new SavePasswordOperator(passwordStore, walletRepository, password);
  }

  public static CompletableOperator completableErrorProxy(Throwable throwable) {
    return new CompletableErrorProxyOperator(throwable);
  }
}
