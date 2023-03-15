package com.asfoundation.wallet.repository;

import com.appcoins.wallet.ui.arch.RxSchedulers;
import com.asfoundation.wallet.entity.PendingTransaction;
import io.reactivex.Observable;
import it.czerwinski.android.hilt.annotations.BoundTo;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by trinkes on 26/02/2018.
 */
@BoundTo(supertype = TrackTransactionService.class) @Named("PendingTransactionService")
public class PendingTransactionService implements TrackTransactionService {
  private final EthereumService service;
  private final RxSchedulers rxSchedulers;

  public @Inject PendingTransactionService(EthereumService service, RxSchedulers rxSchedulers) {
    this.rxSchedulers = rxSchedulers;
    this.service = service;
  }

  @Override public Observable<PendingTransaction> checkTransactionState(String hash) {
    return Observable.interval(5, TimeUnit.SECONDS, rxSchedulers.getIo())
        .timeInterval()
        .switchMap(scan -> service.getTransaction(hash)
            .toObservable())
        .takeUntil(pendingTransaction -> !pendingTransaction.isPending());
  }
}
