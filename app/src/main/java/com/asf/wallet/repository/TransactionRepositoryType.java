package com.asf.wallet.repository;

import com.asf.wallet.entity.Transaction;
import com.asf.wallet.entity.TransactionBuilder;
import com.asf.wallet.entity.Wallet;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface TransactionRepositoryType {
  Observable<Transaction[]> fetchTransaction(Wallet wallet);

  Maybe<Transaction> findTransaction(Wallet wallet, String transactionHash);

  Single<String> createTransaction(TransactionBuilder transactionBuilder, String password);
}
