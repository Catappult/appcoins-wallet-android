package com.asfoundation.contractproxy.proxy;

public interface ProxyContract {
  String getContractAddressById(String fromAddress, int chainId, String id);
}
