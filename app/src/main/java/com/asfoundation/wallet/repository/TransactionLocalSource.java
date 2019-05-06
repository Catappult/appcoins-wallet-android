package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.entity.Wallet;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.util.List;

public interface TransactionLocalSource {
  Single<List<RawTransaction>> fetchTransaction(NetworkInfo networkInfo, Wallet wallet);

  Completable putTransactions(NetworkInfo networkInfo, Wallet wallet,
      RawTransaction[] transactions);

  Single<RawTransaction> findLast(NetworkInfo networkInfo, Wallet wallet);
}
