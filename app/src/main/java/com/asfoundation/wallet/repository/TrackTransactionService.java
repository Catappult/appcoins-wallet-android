package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import io.reactivex.Observable;

public interface TrackTransactionService {
  Observable<PendingTransaction> checkTransactionState(String hash);

  Observable<PendingTransaction> checkTransactionState(String hash, int chainId);
}
