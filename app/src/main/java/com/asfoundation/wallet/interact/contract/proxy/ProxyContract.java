package com.asfoundation.wallet.interact.contract.proxy;

public interface ProxyContract {
  String getContractAddressById(String fromAddress, int chainId, String id);
}
