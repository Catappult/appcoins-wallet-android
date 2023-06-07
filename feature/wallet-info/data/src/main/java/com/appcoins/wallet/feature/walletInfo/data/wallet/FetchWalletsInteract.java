package com.appcoins.wallet.feature.walletInfo.data.wallet;

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet;
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType;
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
