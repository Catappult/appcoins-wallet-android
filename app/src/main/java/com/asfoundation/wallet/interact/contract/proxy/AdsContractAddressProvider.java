package com.asfoundation.wallet.interact.contract.proxy;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import java.util.Map;

public class AdsContractAddressProvider {
  public static final String ADVERTISEMENT_CONTRACT = "advertisement";
  private final WalletAddressProvider walletAddressProvider;
  private final Web3jProxyContract web3jProxyContract;
  private final Scheduler scheduler;
  private final Map<String, String> cache;

  public AdsContractAddressProvider(WalletAddressProvider walletAddressProvider,
      Web3jProxyContract web3jProxyContract, Scheduler scheduler, Map<String, String> cache) {
    this.walletAddressProvider = walletAddressProvider;
    this.web3jProxyContract = web3jProxyContract;
    this.scheduler = scheduler;
    this.cache = cache;
  }

  public Single<String> getAdsAddress(int chainId) {
    return walletAddressProvider.get()
        .observeOn(scheduler)
        .map(wallet -> syncGetAdsContractAddress(chainId, wallet));
  }

  private synchronized String syncGetAdsContractAddress(int chainId, String wallet) {
    String key = ADVERTISEMENT_CONTRACT + chainId;
    if (cache.get(key) == null) {
      cache.put(key,
          web3jProxyContract.getContractAddressById(wallet, chainId, ADVERTISEMENT_CONTRACT));
    }
    return cache.get(key);
  }
}
