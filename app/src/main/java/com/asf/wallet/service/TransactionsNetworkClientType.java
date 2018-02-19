package com.asf.wallet.service;

import com.asf.wallet.entity.Transaction;
import com.asf.wallet.entity.Wallet;
import io.reactivex.Observable;

public interface TransactionsNetworkClientType {
  Observable<Transaction[]> fetchTransactions(String forAddress);

  Observable<Transaction[]> fetchLastTransactions(Wallet wallet, Transaction lastTransaction);
}
