package com.asfoundation.wallet.billing;

import rx.Completable;
import rx.Single;

public interface TransactionService {

  Single<String> createTransaction(String address, String signature, String token);

  Single<String> getSession(String transactionUid);

  Completable finishTransaction(String transactionUid, String paykey);
}
