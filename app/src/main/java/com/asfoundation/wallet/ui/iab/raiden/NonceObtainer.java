package com.asfoundation.wallet.ui.iab.raiden;

import java.io.IOException;
import java.math.BigInteger;
import org.web3j.abi.datatypes.Address;

public class NonceObtainer {
  private final int refreshIntervalMillis;
  private final NonceProvider nonceProvider;
  private final Address address;
  private final Object object = new Object();
  private AtomicBigInteger atomicBigInteger;
  private long refreshTime;

  /**
   * @param refreshIntervalMillis time window between each nonce sync with ethereum network.
   * @param address
   */
  public NonceObtainer(int refreshIntervalMillis, NonceProvider nonceProvider, Address address) {
    this.refreshIntervalMillis = refreshIntervalMillis;
    this.nonceProvider = nonceProvider;
    this.address = address;
  }

  public BigInteger getNonce() {
    synchronized (object) {
      if (atomicBigInteger == null
          || System.currentTimeMillis() - refreshTime > refreshIntervalMillis) {
        refresh();
      }
      return atomicBigInteger.get();
    }
  }

  public boolean consumeNonce(BigInteger nonce) {
    synchronized (object) {
      if (atomicBigInteger == null) {
        throw new IllegalStateException("No nonce was get for the wallet " + address.toString());
      }
      if (atomicBigInteger.get()
          .compareTo(nonce) == 0) {
        atomicBigInteger.increment();
        return true;
      } else {
        return false;
      }
    }
  }

  private void refresh() {
    try {
      refreshTime = System.currentTimeMillis();
      BigInteger count = nonceProvider.getNonce(address);
      if (atomicBigInteger == null
          || atomicBigInteger.get()
          .compareTo(count) < 0) {
        atomicBigInteger = new AtomicBigInteger(count);
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
