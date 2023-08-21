package com.asfoundation.wallet.interact;

import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType;
import io.reactivex.Completable;
import javax.inject.Inject;

public class SetDefaultWalletInteractor {

  private final WalletRepositoryType accountRepository;

  public @Inject SetDefaultWalletInteractor(WalletRepositoryType walletRepositoryType) {
    this.accountRepository = walletRepositoryType;
  }

  public Completable set(String address) {
    return accountRepository.setDefaultWallet(address);
  }
}
