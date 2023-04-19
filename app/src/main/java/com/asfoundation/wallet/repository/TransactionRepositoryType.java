package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.transactions.Transaction;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;

public interface TransactionRepositoryType {

  Single<List<Transaction>> fetchNewTransactions(String wallet);

  Observable<List<Transaction>> fetchTransactions(String wallet);

  Single<String> createTransaction(TransactionBuilder transactionBuilder, String password);

  Single<String> approve(TransactionBuilder transactionBuilder, String password);

  Single<String> callIab(TransactionBuilder transaction, String password);

  Single<String> computeApproveTransactionHash(TransactionBuilder transactionBuilder,
      String password);

  Single<String> computeBuyTransactionHash(TransactionBuilder transactionBuilder, String password);

  void stop();
}
