package com.asf.wallet.interact;

import com.asf.wallet.entity.Wallet;
import com.asf.wallet.repository.WalletRepositoryType;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SetDefaultWalletInteract {

  private WalletRepositoryType accountRepository;

  public SetDefaultWalletInteract(WalletRepositoryType walletRepositoryType) {
    this.accountRepository = walletRepositoryType;
  }

  public Completable set(Wallet wallet) {
    return accountRepository.setDefaultWallet(wallet)
        .observeOn(AndroidSchedulers.mainThread());
  }
}
