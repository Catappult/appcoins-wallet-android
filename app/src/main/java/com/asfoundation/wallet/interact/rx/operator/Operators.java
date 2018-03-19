package com.asfoundation.wallet.interact.rx.operator;

import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.WalletRepositoryType;
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
