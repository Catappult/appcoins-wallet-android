package com.asfoundation.wallet.wallets;

import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import io.reactivex.Single;
import javax.inject.Inject;

public class FetchWalletsInteract {

  private final WalletRepositoryType accountRepository;

  public @Inject FetchWalletsInteract(WalletRepositoryType accountRepository) {
    this.accountRepository = accountRepository;
  }

  public Single<Wallet[]> fetch() {
    return accountRepository.fetchWallets();
  }
}
