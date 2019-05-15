package com.asfoundation.wallet;

import io.reactivex.Completable;

public interface TransactionService {
  Completable waitForTransactionToComplete(String transactionHash);
}
