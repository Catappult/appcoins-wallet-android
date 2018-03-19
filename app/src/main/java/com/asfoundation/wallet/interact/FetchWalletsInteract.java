package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class FetchWalletsInteract {

  private final WalletRepositoryType accountRepository;

  public FetchWalletsInteract(WalletRepositoryType accountRepository) {
    this.accountRepository = accountRepository;
  }

  public Single<Wallet[]> fetch() {
    return accountRepository.fetchWallets()
        .observeOn(AndroidSchedulers.mainThread());
  }
}
