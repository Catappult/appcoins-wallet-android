package com.asfoundation.wallet.repository;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigInteger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;

public class NonceGetter {

  private boolean shouldBump;

  Single<BigInteger> getNonce(Web3j web3j, String fromAddress) {
    return Single.fromCallable(() -> {
      EthGetTransactionCount ethGetTransactionCount =
          web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST)
              .send();
      BigInteger transactionCount = ethGetTransactionCount.getTransactionCount();
      if (shouldBump) {
        transactionCount = transactionCount.add(BigInteger.ONE);
        shouldBump = false;
      }
      return transactionCount;
    })
        .subscribeOn(Schedulers.io());
  }

  public void bump() {
    shouldBump = true;
  }
}
