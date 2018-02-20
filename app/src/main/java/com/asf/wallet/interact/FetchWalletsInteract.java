package com.asf.wallet.interact;

import com.asf.wallet.entity.Wallet;
import com.asf.wallet.repository.WalletRepositoryType;
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
