package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import io.reactivex.Single;

public class FindDefaultWalletInteract {

  private final WalletRepositoryType walletRepository;

  public FindDefaultWalletInteract(WalletRepositoryType walletRepository) {
    this.walletRepository = walletRepository;
  }

  public Single<Wallet> find() {
    return walletRepository.getDefaultWallet()
        .onErrorResumeNext(throwable -> walletRepository.fetchWallets()
            .filter(wallets -> wallets.length > 0)
            .map(wallets -> wallets[0])
            .flatMapCompletable(walletRepository::setDefaultWallet)
            .andThen(walletRepository.getDefaultWallet()));
  }
}
