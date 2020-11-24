package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.repository.WalletRepositoryType;
import io.reactivex.Completable;

public class SetDefaultWalletInteractor {

  private WalletRepositoryType accountRepository;

  public SetDefaultWalletInteractor(WalletRepositoryType walletRepositoryType) {
    this.accountRepository = walletRepositoryType;
  }

  public Completable set(String address) {
    return accountRepository.setDefaultWallet(address);
  }
}
