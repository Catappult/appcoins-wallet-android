package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.repository.TransactionRepositoryType;
import com.asfoundation.wallet.transactions.Transaction;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

public class FetchTransactionsInteract {

  private final TransactionRepositoryType transactionRepository;

  public FetchTransactionsInteract(TransactionRepositoryType transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Observable<List<Transaction>> fetch(String wallet) {
    return transactionRepository.fetchTransaction(wallet)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }

  public void stop() {
    transactionRepository.stop();
  }
}
