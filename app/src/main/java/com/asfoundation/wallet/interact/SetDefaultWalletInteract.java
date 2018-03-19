package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.WalletRepositoryType;
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
