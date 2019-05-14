package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.transactions.TransactionsMapper;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import java.util.List;

public class OffChainTransactions {
  private final OffChainTransactionsRepository repository;
  private final TransactionsMapper mapper;
  private final String versionCode;
  private Scheduler scheduler;

  public OffChainTransactions(OffChainTransactionsRepository repository, TransactionsMapper mapper,
      String versionCode, Scheduler scheduler) {
    this.repository = repository;
    this.mapper = mapper;
    this.versionCode = versionCode;
    this.scheduler = scheduler;
  }

  public Single<List<Transaction>> getTransactions(String wallet, boolean offChainOnly) {
    return repository.getTransactions(wallet, versionCode, offChainOnly)
        .subscribeOn(scheduler)
        .flatMap(channelHistoryResponse -> mapper.mapFromWalletHistory(
            channelHistoryResponse.getResult()));
  }
}
