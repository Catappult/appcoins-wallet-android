package com.asf.wallet.repository;

import com.asf.wallet.entity.PendingTransaction;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import java.util.concurrent.TimeUnit;

/**
 * Created by trinkes on 26/02/2018.
 */

public class PendingTransactionService {
  private final EthereumService service;
  private final int period;
  private final Scheduler scheduler;

  public PendingTransactionService(EthereumService service, Scheduler scheduler, int period) {
    this.scheduler = scheduler;
    this.service = service;
    this.period = period;
  }

  public Observable<PendingTransaction> checkTransactionState(String hash) {
    return Observable.interval(period, TimeUnit.SECONDS, scheduler)
        .timeInterval()
        .switchMap(scan -> service.getTransaction(hash)
            .toObservable())
        .takeUntil(pendingTransaction -> !pendingTransaction.isPending());
  }
}
