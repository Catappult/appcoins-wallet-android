package com.asfoundation.wallet.ui.iab.raiden;

import org.web3j.abi.datatypes.Address;

public class NonceObtainerFactory {
  private final int refreshIntervalMillis;
  private final NonceProvider nonceProvider;

  public NonceObtainerFactory(int refreshIntervalMillis, NonceProvider nonceProvider) {
    this.refreshIntervalMillis = refreshIntervalMillis;
    this.nonceProvider = nonceProvider;
  }

  public NonceObtainer build(Address address) {
    return new NonceObtainer(refreshIntervalMillis, nonceProvider, address);
  }
}
