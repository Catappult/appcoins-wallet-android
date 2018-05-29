package com.asfoundation.wallet.interact.contract.proxy;

import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class AdsContractAddressProvider {
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final Web3jProxyContract web3jProxyContract;

  public AdsContractAddressProvider(FindDefaultWalletInteract findDefaultWalletInteract,
      Web3jProxyContract web3jProxyContract) {
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.web3jProxyContract = web3jProxyContract;
  }

  public Single<String> getAdsAddress(int chainId) {
    return findDefaultWalletInteract.find()
        .observeOn(Schedulers.io())
        .map(wallet -> web3jProxyContract.getContractAddressById(wallet.address, chainId,
            "advertisement"));
  }
}
