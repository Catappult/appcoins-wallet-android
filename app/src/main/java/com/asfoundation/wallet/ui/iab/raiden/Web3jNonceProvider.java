package com.asfoundation.wallet.ui.iab.raiden;

import com.asfoundation.wallet.repository.Web3jProvider;
import java.io.IOException;
import java.math.BigInteger;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.core.DefaultBlockParameterName;

public class Web3jNonceProvider implements NonceProvider {
  private final Web3jProvider web3jProvider;

  public Web3jNonceProvider(Web3jProvider web3jProvider) {
    this.web3jProvider = web3jProvider;
  }

  @Override public BigInteger getNonce(Address address) throws IOException {
    return web3jProvider.getDefault()
        .ethGetTransactionCount(address.toString(), DefaultBlockParameterName.PENDING)
        .send()
        .getTransactionCount();
  }
}
