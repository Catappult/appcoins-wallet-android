package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.analytics.AmplitudeAnalytics;
import com.asfoundation.wallet.analytics.AnalyticsSetup;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.service.AccountKeystoreService;
import com.asfoundation.wallet.service.WalletBalanceService;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import java.math.BigDecimal;
import org.jetbrains.annotations.NotNull;

public class WalletRepository implements WalletRepositoryType {

  private final PreferencesRepositoryType preferencesRepositoryType;
  private final AccountKeystoreService accountKeystoreService;
  private final WalletBalanceService walletBalanceService;
  private final Scheduler networkScheduler;
  private final AnalyticsSetup analyticsSetUp;
  private final AmplitudeAnalytics amplitudeAnalytics;

  public WalletRepository(PreferencesRepositoryType preferencesRepositoryType,
      AccountKeystoreService accountKeystoreService, WalletBalanceService walletBalanceService,
      Scheduler networkScheduler, AnalyticsSetup analyticsSetUp,
      AmplitudeAnalytics amplitudeAnalytics) {
    this.preferencesRepositoryType = preferencesRepositoryType;
    this.accountKeystoreService = accountKeystoreService;
    this.walletBalanceService = walletBalanceService;
    this.networkScheduler = networkScheduler;
    this.analyticsSetUp = analyticsSetUp;
    this.amplitudeAnalytics = amplitudeAnalytics;
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
  public Single<Wallet> restoreKeystoreToWallet(String store, String password, String newPassword) {
    return accountKeystoreService.restoreKeystore(store, password, newPassword);
  }

  @Override public Single<Wallet> restorePrivateKeyToWallet(String privateKey, String newPassword) {
    return accountKeystoreService.restorePrivateKey(privateKey, newPassword);
  }

  @Override
  public Single<String> exportWallet(String address, String password, String newPassword) {
    return accountKeystoreService.exportAccount(address, password, newPassword);
  }

  @Override public Completable deleteWallet(String address, String password) {
    return accountKeystoreService.deleteAccount(address, password);
  }

  @Override public Completable setDefaultWallet(String address) {
    return Completable.fromAction(() -> {
      analyticsSetUp.setUserId(address);
      amplitudeAnalytics.setUserId(address);
      preferencesRepositoryType.setCurrentWalletAddress(address);
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

  @NotNull @Override public Single<BigDecimal> getEthBalanceInWei(String address) {
    return walletBalanceService.getWalletBalance(address)
        .map(walletBalance -> new BigDecimal(walletBalance.getEth()))
        .subscribeOn(networkScheduler);
  }

  @NotNull @Override public Single<BigDecimal> getAppcBalanceInWei(String address) {
    return walletBalanceService.getWalletBalance(address)
        .map(walletBalance -> new BigDecimal(walletBalance.getAppc()))
        .subscribeOn(networkScheduler);
  }
}