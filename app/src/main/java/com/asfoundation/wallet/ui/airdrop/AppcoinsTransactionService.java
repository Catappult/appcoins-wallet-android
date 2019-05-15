package com.asfoundation.wallet.ui.airdrop;

import com.asfoundation.wallet.TransactionService;
import com.asfoundation.wallet.repository.PendingTransactionService;
import com.asfoundation.wallet.repository.TransactionNotFoundException;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.concurrent.TimeUnit;

public class AppcoinsTransactionService implements TransactionService {
  private final PendingTransactionService pendingTransactionService;

  public AppcoinsTransactionService(PendingTransactionService pendingTransactionService) {
    this.pendingTransactionService = pendingTransactionService;
  }

  @Override public Completable waitForTransactionToComplete(String transactionHash) {
    return pendingTransactionService.checkTransactionState(transactionHash)
        .retryWhen(throwableObservable -> throwableObservable.flatMap(throwable -> {
          if (throwable instanceof TransactionNotFoundException) {
            return Observable.timer(5, TimeUnit.SECONDS);
          }
          return Observable.error(throwable);
        }))
        .ignoreElements();
  }
}
