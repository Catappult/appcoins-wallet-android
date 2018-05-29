package com.asfoundation.wallet.interact.contract.proxy;

import java.io.IOException;

public interface ProxyContract {
  String getContractAddressById(String fromAddress, int chainId, String id) throws IOException;
}
