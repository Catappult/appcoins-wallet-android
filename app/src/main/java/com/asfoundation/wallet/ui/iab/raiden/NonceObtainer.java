package com.asfoundation.wallet.ui.iab.raiden;

import com.asf.microraidenj.type.Address;
import java.io.IOException;
import java.math.BigInteger;

public class NonceObtainer implements com.asf.microraidenj.eth.NonceObtainer {
  private final int refreshIntervalMillis;
  private final NonceProvider nonceProvider;
  private final Object object = new Object();
  private AtomicBigInteger atomicBigInteger;
  private long refreshTime;

  /**
   * @param refreshIntervalMillis time window between each nonce sync with ethereum network.
   */
  public NonceObtainer(int refreshIntervalMillis, NonceProvider nonceProvider) {
    this.refreshIntervalMillis = refreshIntervalMillis;
    this.nonceProvider = nonceProvider;
  }

  @Override public BigInteger getNonce(Address address) {
    synchronized (object) {
      if (atomicBigInteger == null
          || System.currentTimeMillis() - refreshTime > refreshIntervalMillis) {
        refresh(address);
      }
      return atomicBigInteger.get();
    }
  }

  public boolean consumeNonce(BigInteger nonce) {
    synchronized (object) {
      if (atomicBigInteger.get()
          .compareTo(nonce) == 0) {
        atomicBigInteger.increment();
        return true;
      } else {
        return false;
      }
    }
  }

  private void refresh(Address address) {
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
