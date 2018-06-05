package com.asfoundation.contractproxy.proxy;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import java.util.Map;

public class ContractAddressProvider {
  public static final String ADVERTISEMENT_CONTRACT_ID = "advertisement";
  private final WalletAddressProvider walletAddressProvider;
  private final Web3jProxyContract web3jProxyContract;
  private final Scheduler scheduler;
  private final Map<String, String> cache;

  public ContractAddressProvider(WalletAddressProvider walletAddressProvider,
      Web3jProxyContract web3jProxyContract, Scheduler scheduler, Map<String, String> cache) {
    this.walletAddressProvider = walletAddressProvider;
    this.web3jProxyContract = web3jProxyContract;
    this.scheduler = scheduler;
    this.cache = cache;
  }

  public Single<String> getAdsAddress(int chainId) {
    return getAddress(chainId, ADVERTISEMENT_CONTRACT_ID);
  }

  private Single<String> getAddress(int chainId, String contractId) {
    return walletAddressProvider.get()
        .observeOn(scheduler)
        .map(wallet -> syncGetContractAddress(chainId, wallet, contractId));
  }

  private synchronized String syncGetContractAddress(int chainId, String wallet,
      String contractId) {
    String key = contractId + chainId;
    if (cache.get(key) == null) {
      cache.put(key, web3jProxyContract.getContractAddressById(wallet, chainId, contractId));
    }
    return cache.get(key);
  }
}
