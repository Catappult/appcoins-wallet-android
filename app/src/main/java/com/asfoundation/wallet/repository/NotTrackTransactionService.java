package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import io.reactivex.Observable;

public class NotTrackTransactionService implements TrackTransactionService {
  @Override public Observable<PendingTransaction> checkTransactionState(String hash) {
    return Observable.just(new PendingTransaction(hash, false));
  }
}
