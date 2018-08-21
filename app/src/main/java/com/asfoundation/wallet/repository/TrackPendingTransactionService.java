package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import io.reactivex.Observable;

public class TrackPendingTransactionService implements TrackTransactionService {
  private final PendingTransactionService trackTransactionService;

  public TrackPendingTransactionService(PendingTransactionService trackTransactionService) {
    this.trackTransactionService = trackTransactionService;
  }

  @Override public Observable<PendingTransaction> checkTransactionState(String hash) {
    return trackTransactionService.checkTransactionState(hash)
        .firstOrError()
        .toObservable();
  }

  @Override public Observable<PendingTransaction> checkTransactionState(String hash, int chainId) {
    return trackTransactionService.checkTransactionState(hash, chainId)
        .firstOrError()
        .toObservable();
  }
}
