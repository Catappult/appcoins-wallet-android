package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.analytics.AnalyticsSetUp;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.service.AccountKeystoreService;
import com.asfoundation.wallet.service.WalletBalanceService;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import java.math.BigDecimal;

public class WalletRepository implements WalletRepositoryType {

  private final PreferencesRepositoryType preferencesRepositoryType;
  private final AccountKeystoreService accountKeystoreService;
  private final WalletBalanceService walletBalanceService;
  private final Scheduler networkScheduler;
  private final AnalyticsSetUp analyticsSetUp;

  public WalletRepository(PreferencesRepositoryType preferencesRepositoryType,
      AccountKeystoreService accountKeystoreService, WalletBalanceService walletBalanceService,
      Scheduler networkScheduler, AnalyticsSetUp analyticsSetUp) {
    this.preferencesRepositoryType = preferencesRepositoryType;
    this.accountKeystoreService = accountKeystoreService;
    this.walletBalanceService = walletBalanceService;
    this.networkScheduler = networkScheduler;
    this.analyticsSetUp = analyticsSetUp;
  }

  @Override public Single<Wallet[]> fetchWallets() {
    return accountKeystoreService.fetchAccounts();
  }

  @Override public Single<Wallet> findWallet(String address) {
    return fetchWallets().flatMap(accounts -> {
      for (Wallet wallet : accounts) {
        if (wallet.sameAddress(address)) {
          return Single.just(wallet);
        }
      }
      return null;
    });
  }

  @Override public Single<Wallet> createWallet(String password) {
    return accountKeystoreService.createAccount(password);
  }

  @Override
  public Single<Wallet> importKeystoreToWallet(String store, String password, String newPassword) {
    return accountKeystoreService.importKeystore(store, password, newPassword);
  }

  @Override public Single<Wallet> importPrivateKeyToWallet(String privateKey, String newPassword) {
    return accountKeystoreService.importPrivateKey(privateKey, newPassword);
  }

  @Override public Single<String> exportWallet(Wallet wallet, String password, String newPassword) {
    return accountKeystoreService.exportAccount(wallet, password, newPassword);
  }

  @Override public Completable deleteWallet(String address, String password) {
    return accountKeystoreService.deleteAccount(address, password);
  }

  @Override public Completable setDefaultWallet(Wallet wallet) {
    return Completable.fromAction(() -> {
      preferencesRepositoryType.setCurrentWalletAddress(wallet.address);
      analyticsSetUp.setUserId(wallet.address);
    });
  }

  @Override public Single<Wallet> getDefaultWallet() {
    return Single.fromCallable(() -> {
      String currentWalletAddress = preferencesRepositoryType.getCurrentWalletAddress();
      if (currentWalletAddress == null) {
        throw new WalletNotFoundException();
      } else {
        return currentWalletAddress;
      }
    })
        .flatMap(this::findWallet);
  }

  @Override public Single<BigDecimal> getEthBalanceInWei(Wallet wallet) {
    return walletBalanceService.getWalletBalance(wallet.address)
        .map(walletBalance -> new BigDecimal(walletBalance.getEth()))
        .subscribeOn(networkScheduler);
  }

  @Override public Single<BigDecimal> getAppcBalanceInWei(Wallet wallet) {
    return walletBalanceService.getWalletBalance(wallet.address)
        .map(walletBalance -> new BigDecimal(walletBalance.getAppc()))
        .subscribeOn(networkScheduler);
  }
}