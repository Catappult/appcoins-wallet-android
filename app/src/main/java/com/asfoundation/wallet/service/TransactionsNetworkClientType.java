package com.asfoundation.wallet.service;

import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.entity.Wallet;
import io.reactivex.Observable;

public interface TransactionsNetworkClientType {
  Observable<RawTransaction[]> fetchTransactions(String forAddress);

  Observable<RawTransaction[]> fetchLastTransactions(Wallet wallet, RawTransaction lastTransaction,
      NetworkInfo networkInfo);
}
