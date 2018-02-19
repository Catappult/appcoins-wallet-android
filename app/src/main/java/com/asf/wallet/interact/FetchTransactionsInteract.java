package com.asf.wallet.interact;

import com.asf.wallet.entity.Transaction;
import com.asf.wallet.entity.Wallet;
import com.asf.wallet.repository.TransactionRepositoryType;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FetchTransactionsInteract {

  private final TransactionRepositoryType transactionRepository;

  public FetchTransactionsInteract(TransactionRepositoryType transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Observable<Transaction[]> fetch(Wallet wallet) {
    return transactionRepository.fetchTransaction(wallet)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }
}
