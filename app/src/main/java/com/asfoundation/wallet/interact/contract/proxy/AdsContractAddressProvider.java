package com.asfoundation.wallet.interact.contract.proxy;

import io.reactivex.Scheduler;
import io.reactivex.Single;

public class AdsContractAddressProvider {
  private final WalletAddressProvider walletAddressProvider;
  private final Web3jProxyContract web3jProxyContract;
  private final Scheduler scheduler;

  public AdsContractAddressProvider(WalletAddressProvider walletAddressProvider,
      Web3jProxyContract web3jProxyContract, Scheduler scheduler) {
    this.walletAddressProvider = walletAddressProvider;
    this.web3jProxyContract = web3jProxyContract;
    this.scheduler = scheduler;
  }

  public Single<String> getAdsAddress(int chainId) {
    return walletAddressProvider.get()
        .observeOn(scheduler)
        .map(wallet -> web3jProxyContract.getContractAddressById(wallet, chainId, "advertisement"));
  }
}
