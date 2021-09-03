package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import io.reactivex.Single;

public class FetchWalletsInteract {

  private final WalletRepositoryType accountRepository;

  public FetchWalletsInteract(WalletRepositoryType accountRepository) {
    this.accountRepository = accountRepository;
  }

  public Single<Wallet[]> fetch() {
    return accountRepository.fetchWallets();
  }
}
