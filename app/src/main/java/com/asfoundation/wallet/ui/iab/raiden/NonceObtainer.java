package com.asfoundation.wallet.ui.iab.raiden;

import com.asf.microraidenj.type.Address;
import com.asfoundation.wallet.repository.Web3jProvider;
import java.io.IOException;
import java.math.BigInteger;
import org.web3j.protocol.core.DefaultBlockParameterName;

public class NonceObtainer implements com.asf.microraidenj.eth.NonceObtainer {
  private final int refreshIntervalMillis;
  private final Web3jProvider web3jProvider;
  private AtomicBigInteger atomicBigInteger;
  private long refreshTime;

  public NonceObtainer(int refreshIntervalMillis, Web3jProvider web3jProvider) {
    this.refreshIntervalMillis = refreshIntervalMillis;
    this.web3jProvider = web3jProvider;
  }

  @Override synchronized public BigInteger getNonce(Address address) {
    if (atomicBigInteger == null
        || System.currentTimeMillis() - refreshTime > refreshIntervalMillis) {
      refresh(address);
    }
    return atomicBigInteger.getAndIncrement();
  }

  private void refresh(Address address) {
    try {
      refreshTime = System.currentTimeMillis();
      BigInteger count = web3jProvider.getDefault()
          .ethGetTransactionCount(address.toString(), DefaultBlockParameterName.PENDING)
          .send()
          .getTransactionCount();
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
