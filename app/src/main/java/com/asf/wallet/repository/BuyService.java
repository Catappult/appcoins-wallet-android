package com.asf.wallet.repository;

import com.asf.wallet.entity.PendingTransaction;
import com.asf.wallet.interact.SendTransactionInteract;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

/**
 * Created by trinkes on 3/16/18.
 */

public class BuyService {
  private final SendTransactionInteract sendTransactionInteract;
  private final PendingTransactionService pendingTransactionService;
  private final Cache<String, PaymentTransaction> cache;

  public BuyService(SendTransactionInteract sendTransactionInteract,
      PendingTransactionService pendingTransactionService,
      Cache<String, PaymentTransaction> cache) {
    this.sendTransactionInteract = sendTransactionInteract;
    this.pendingTransactionService = pendingTransactionService;
    this.cache = cache;
  }

  public void start() {
    cache.getAll()
        .flatMap(paymentTransactions -> Observable.fromIterable(paymentTransactions))
        .filter(paymentTransaction -> paymentTransaction.getState()
            .equals(PaymentTransaction.PaymentState.APPROVED))
        .flatMapCompletable(this::buy)
        .subscribe();
  }

  private Completable buy(PaymentTransaction paymentTransaction) {
    return cache.save(paymentTransaction.getUri(),
        new PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.BUYING))
        .andThen(sendTransactionInteract.buy(paymentTransaction.getTransactionBuilder())
            .flatMapCompletable(hash -> pendingTransactionService.checkTransactionState(hash)
                .flatMapCompletable(pendingTransaction -> saveTransaction(pendingTransaction,
                    paymentTransaction))));
  }

  private Completable saveTransaction(PendingTransaction pendingTransaction,
      PaymentTransaction paymentTransaction) {
    if (pendingTransaction.isPending()) {
      return cache.save(paymentTransaction.getUri(),
          new PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.BUYING,
              paymentTransaction.getApproveHash(), pendingTransaction.getHash()));
    }
    return cache.save(paymentTransaction.getUri(),
        new PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.BOUGHT,
            paymentTransaction.getApproveHash(), pendingTransaction.getHash()));
  }

  public Completable buy(String key, PaymentTransaction paymentTransaction) {
    return cache.save(key,
        new PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.APPROVED));
  }

  public Observable<PaymentTransaction> getBuy(String uri) {
    return cache.get(uri);
  }

  public Observable<List<PaymentTransaction>> getAll() {
    return cache.getAll();
  }

  public Completable remove(String key) {
    return cache.remove(key);
  }
}
