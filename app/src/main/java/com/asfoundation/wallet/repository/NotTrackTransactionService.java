package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import io.reactivex.Observable;
import it.czerwinski.android.hilt.annotations.BoundTo;
import javax.inject.Inject;
import javax.inject.Named;

@BoundTo(supertype = TrackTransactionService.class) @Named("NotTrackTransactionService")
public class NotTrackTransactionService implements TrackTransactionService {

  public @Inject NotTrackTransactionService() {
  }

  @Override public Observable<PendingTransaction> checkTransactionState(String hash) {
    return Observable.just(new PendingTransaction(hash, false));
  }
}
