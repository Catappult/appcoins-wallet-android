package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.entity.Wallet;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface TransactionRepositoryType {
  Observable<RawTransaction[]> fetchTransaction(Wallet wallet);

  Maybe<RawTransaction> findTransaction(Wallet wallet, String transactionHash);

  Single<String> createTransaction(TransactionBuilder transactionBuilder, String password);

  Single<String> approve(TransactionBuilder transactionBuilder, String password);

  Single<String> callIab(TransactionBuilder transaction, String password);

  Single<String> computeApproveTransactionHash(TransactionBuilder transactionBuilder,
      String password);

  Single<String> computeBuyTransactionHash(TransactionBuilder transactionBuilder, String password);
}
