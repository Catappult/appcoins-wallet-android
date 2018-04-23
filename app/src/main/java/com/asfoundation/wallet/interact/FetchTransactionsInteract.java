package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.TransactionRepositoryType;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FetchTransactionsInteract {

  private final TransactionRepositoryType transactionRepository;

  public FetchTransactionsInteract(TransactionRepositoryType transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Observable<RawTransaction[]> fetch(Wallet wallet) {
    return transactionRepository.fetchTransaction(wallet)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }
}
