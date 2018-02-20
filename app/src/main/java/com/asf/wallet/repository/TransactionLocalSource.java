package com.asf.wallet.repository;

import com.asf.wallet.entity.NetworkInfo;
import com.asf.wallet.entity.Transaction;
import com.asf.wallet.entity.Wallet;
import io.reactivex.Completable;
import io.reactivex.Single;

public interface TransactionLocalSource {
  Single<Transaction[]> fetchTransaction(NetworkInfo networkInfo, Wallet wallet);

  Completable putTransactions(NetworkInfo networkInfo, Wallet wallet, Transaction[] transactions);

  Single<Transaction> findLast(NetworkInfo networkInfo, Wallet wallet);
}
