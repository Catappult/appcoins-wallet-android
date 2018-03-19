package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.repository.TokenRepositoryType;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class AddTokenInteract {
  private final TokenRepositoryType tokenRepository;
  private final WalletRepositoryType walletRepository;

  public AddTokenInteract(WalletRepositoryType walletRepository,
      TokenRepositoryType tokenRepository) {
    this.walletRepository = walletRepository;
    this.tokenRepository = tokenRepository;
  }

  public Completable add(String address, String symbol, int decimals) {
    return walletRepository.getDefaultWallet()
        .flatMapCompletable(
            wallet -> tokenRepository.addToken(wallet, address, symbol, decimals, true)
                .observeOn(AndroidSchedulers.mainThread()));
  }
}
