package com.asf.wallet.interact.rx.operator;

import com.asf.wallet.entity.Wallet;
import com.asf.wallet.repository.PasswordStore;
import com.asf.wallet.repository.WalletRepositoryType;
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
