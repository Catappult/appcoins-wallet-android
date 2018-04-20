package com.asfoundation.wallet.repository;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigInteger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;

public class NonceGetter {

  private static final int BUMP_TIME_THRESHOLD_IN_MILLIS = 5000;
  private boolean shouldBump;
  private long timeStamp;

  Single<BigInteger> getNonce(Web3j web3j, String fromAddress) {
    return Single.fromCallable(() -> {
      EthGetTransactionCount ethGetTransactionCount =
          web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST)
              .send();
      BigInteger transactionCount = ethGetTransactionCount.getTransactionCount();
      if (shouldBump && System.currentTimeMillis() - timeStamp < BUMP_TIME_THRESHOLD_IN_MILLIS) {
        transactionCount = transactionCount.add(BigInteger.ONE);
        shouldBump = false;
      }
      return transactionCount;
    })
        .subscribeOn(Schedulers.io());
  }

  public void bump() {
    timeStamp = System.currentTimeMillis();
    shouldBump = true;
  }
}
