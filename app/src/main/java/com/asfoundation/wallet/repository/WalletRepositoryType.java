package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.Wallet;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.math.BigDecimal;

public interface WalletRepositoryType {
  Single<Wallet[]> fetchWallets();

  Single<Wallet> findWallet(String address);

  Single<Wallet> createWallet(String password);

  Single<Wallet> importKeystoreToWallet(String store, String password, String newPassword);

  Single<Wallet> importPrivateKeyToWallet(String privateKey, String newPassword);

  Single<String> exportWallet(Wallet wallet, String password, String newPassword);

  Completable deleteWallet(String address, String password);

  Completable setDefaultWallet(String address);

  Single<Wallet> getDefaultWallet();

  Single<BigDecimal> getEthBalanceInWei(String address);

  Single<BigDecimal> getAppcBalanceInWei(String address);
}
