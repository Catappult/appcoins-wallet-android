package com.asfoundation.wallet.interact;

import android.content.SharedPreferences;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Delete and fetchTokens wallets
 */
public class DeleteWalletInteract {
  private static final String WALLET_VERIFIED = "wallet_verified_";
  private final WalletRepositoryType walletRepository;
  private final PasswordStore passwordStore;
  private final SharedPreferences sharedPreferences;

  public DeleteWalletInteract(WalletRepositoryType walletRepository, PasswordStore passwordStore,
      SharedPreferences sharedPreferences) {
    this.walletRepository = walletRepository;
    this.passwordStore = passwordStore;
    this.sharedPreferences = sharedPreferences;
  }

  public Single<Wallet[]> delete(Wallet wallet) {
    return passwordStore.getPassword(wallet)
        .flatMapCompletable(password -> walletRepository.deleteWallet(wallet.address, password))
        .andThen(Completable.fromAction(() -> sharedPreferences.edit()
            .remove(WALLET_VERIFIED + wallet.address)
            .apply()))
        .andThen(walletRepository.fetchWallets())
        .observeOn(AndroidSchedulers.mainThread());
  }
}
