package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.Transaction;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.entity.Wallet;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface TransactionRepositoryType {
  Observable<Transaction[]> fetchTransaction(Wallet wallet);

  Maybe<Transaction> findTransaction(Wallet wallet, String transactionHash);

  Single<String> createTransaction(TransactionBuilder transactionBuilder, String password);

  Single<String> approve(TransactionBuilder transactionBuilder, String password, String spender);

  Single<String> callIab(TransactionBuilder transaction, String password, String contractAddress);
}
