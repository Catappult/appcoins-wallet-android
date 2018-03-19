package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Transaction;
import com.asfoundation.wallet.entity.Wallet;
import io.reactivex.Completable;
import io.reactivex.Single;

public interface TransactionLocalSource {
  Single<Transaction[]> fetchTransaction(NetworkInfo networkInfo, Wallet wallet);

  Completable putTransactions(NetworkInfo networkInfo, Wallet wallet, Transaction[] transactions);

  Single<Transaction> findLast(NetworkInfo networkInfo, Wallet wallet);
}
