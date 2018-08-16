package com.asfoundation.wallet.billing;

import rx.Completable;
import rx.Single;

public interface TransactionService {

  Single<String> createTransaction(String address, String signature, String token,
      String packageName, String payload, String productName, String developerWallet, String storeWallet);

  Single<String> getSession(String address, String signature, String transactionUid);

  Completable finishTransaction(String address, String signature, String transactionUid, String paykey);
}
