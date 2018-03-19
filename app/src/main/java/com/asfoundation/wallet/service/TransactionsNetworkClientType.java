package com.asfoundation.wallet.service;

import com.asfoundation.wallet.entity.Transaction;
import com.asfoundation.wallet.entity.Wallet;
import io.reactivex.Observable;

public interface TransactionsNetworkClientType {
  Observable<Transaction[]> fetchTransactions(String forAddress);

  Observable<Transaction[]> fetchLastTransactions(Wallet wallet, Transaction lastTransaction);
}
