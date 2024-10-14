package com.appcoins.wallet.feature.walletInfo.data.authentication.rxOperator;

import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore;
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet;
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;

import static com.appcoins.wallet.feature.walletInfo.data.authentication.rxOperator.Operators.completableErrorProxy;

public class SavePasswordOperator implements SingleTransformer<Wallet, Wallet> {

  private final PasswordStore passwordStore;
  private final String password;
  private final WalletRepositoryType walletRepository;

  SavePasswordOperator(PasswordStore passwordStore, WalletRepositoryType walletRepository,
      String password) {
    this.passwordStore = passwordStore;
    this.password = password;
    this.walletRepository = walletRepository;
  }

  @Override public Single<Wallet> apply(Single<Wallet> upstream) {
    return upstream.flatMap(wallet -> passwordStore.setPassword(wallet.getAddress(), password)
        .onErrorResumeNext(err -> walletRepository.deleteWallet(wallet.getAddress(), password)
            .lift(completableErrorProxy(err)))
        .toSingle(() -> wallet));
  }
}
