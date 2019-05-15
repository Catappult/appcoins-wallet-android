package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.service.AccountKeystoreService;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;

public class WalletRepository implements WalletRepositoryType {

  private final PreferenceRepositoryType preferenceRepositoryType;
  private final AccountKeystoreService accountKeystoreService;
  private final Web3jProvider web3jProvider;

  public WalletRepository(PreferenceRepositoryType preferenceRepositoryType,
      AccountKeystoreService accountKeystoreService, Web3jProvider web3jProvider) {
    this.preferenceRepositoryType = preferenceRepositoryType;
    this.accountKeystoreService = accountKeystoreService;
    this.web3jProvider = web3jProvider;
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
    return Completable.fromAction(
        () -> preferenceRepositoryType.setCurrentWalletAddress(wallet.address));
  }

  @Override public Single<Wallet> getDefaultWallet() {
    return Single.fromCallable(() -> {
      String currentWalletAddress = preferenceRepositoryType.getCurrentWalletAddress();
      if (currentWalletAddress == null) {
        throw new WalletNotFoundException();
      } else {
        return currentWalletAddress;
      }
    })
        .flatMap(this::findWallet);
  }

  @Override public Single<BigDecimal> balanceInWei(Wallet wallet) {
    Web3j web3j = web3jProvider.get();
    return Single.fromCallable(() -> new BigDecimal(
        web3j.ethGetBalance(wallet.address, DefaultBlockParameterName.LATEST)
            .send()
            .getBalance()))
        .subscribeOn(Schedulers.io());
  }
}