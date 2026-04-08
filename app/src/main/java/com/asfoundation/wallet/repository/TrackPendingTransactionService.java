package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import io.reactivex.Observable;
import javax.inject.Inject;

public class TrackPendingTransactionService implements TrackTransactionService {
  private final PendingTransactionService trackTransactionService;

  public @Inject TrackPendingTransactionService(PendingTransactionService trackTransactionService) {
    this.trackTransactionService = trackTransactionService;
  }

  @Override public Observable<PendingTransaction> checkTransactionState(String hash) {
    return trackTransactionService.checkTransactionState(hash)
        .firstOrError()
        .toObservable();
  }
}
