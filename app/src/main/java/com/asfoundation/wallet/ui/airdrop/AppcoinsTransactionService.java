package com.asfoundation.wallet.ui.airdrop;

import com.asfoundation.wallet.TransactionService;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.PendingTransactionService;
import com.asfoundation.wallet.repository.TransactionNotFoundException;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.concurrent.TimeUnit;

public class AppcoinsTransactionService implements TransactionService {
  private final PendingTransactionService pendingTransactionService;
  private final EthereumNetworkRepositoryType repository;

  public AppcoinsTransactionService(PendingTransactionService pendingTransactionService,
      EthereumNetworkRepositoryType repository) {
    this.pendingTransactionService = pendingTransactionService;
    this.repository = repository;
  }

  @Override public Completable waitForTransactionToComplete(String transactionHash, int chainId) {
    return Completable.fromAction(() -> setNetwork(chainId))
        .andThen(pendingTransactionService.checkTransactionState(transactionHash))
        .retryWhen(throwableObservable -> throwableObservable.flatMap(throwable -> {
          if (throwable instanceof TransactionNotFoundException) {
            return Observable.timer(5, TimeUnit.SECONDS);
          }
          return Observable.error(throwable);
        }))
        .ignoreElements();
  }

  public void setNetwork(int chainId) {
    for (NetworkInfo networkInfo : repository.getAvailableNetworkList()) {
      if (chainId == networkInfo.chainId) {
        repository.setDefaultNetworkInfo(networkInfo);
      }
    }
  }
}
