package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.Wallet;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.math.BigDecimal;

public interface WalletRepositoryType {
  Single<Wallet[]> fetchWallets();

  Single<Wallet> findWallet(String address);

  Single<Wallet> createWallet(String password);

  Single<Wallet> restoreKeystoreToWallet(String store, String password, String newPassword);

  Single<Wallet> restorePrivateKeyToWallet(String privateKey, String newPassword);

  Single<String> exportWallet(String address, String password, String newPassword);

  Completable deleteWallet(String address, String password);

  Completable setDefaultWallet(String address);

  Single<Wallet> getDefaultWallet();

  Single<BigDecimal> getEthBalanceInWei(String address);

  Single<BigDecimal> getAppcBalanceInWei(String address);
}
