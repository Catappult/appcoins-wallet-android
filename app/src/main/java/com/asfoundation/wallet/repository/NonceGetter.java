package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigInteger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.http.HttpService;

public class NonceGetter {

  private static final int BUMP_TIME_THRESHOLD_IN_MILLIS = 5000;
  private final EthereumNetworkRepositoryType networkRepository;
  private final FindDefaultWalletInteract defaultWalletInteract;
  private boolean shouldBump;
  private long timeStamp;

  public NonceGetter(EthereumNetworkRepositoryType networkRepository,
      FindDefaultWalletInteract defaultWalletInteract) {
    this.networkRepository = networkRepository;
    this.defaultWalletInteract = defaultWalletInteract;
  }

  Single<BigInteger> getNonce(String fromAddress) {
    final Web3j web3j =
        Web3jFactory.build(new HttpService(networkRepository.getDefaultNetwork().rpcServerUrl));
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

  public Single<BigInteger> getNonce() {
    return defaultWalletInteract.find()
        .flatMap(wallet -> getNonce(wallet.address));
  }
}
