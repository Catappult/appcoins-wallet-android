package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
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
        .flatMapCompletable(paymentTransactions -> Observable.fromIterable(paymentTransactions)
            .filter(paymentTransaction -> paymentTransaction.getState()
                .equals(PaymentTransaction.PaymentState.APPROVED))
            .flatMapCompletable(paymentTransaction -> cache.save(paymentTransaction.getUri(),
                new PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.BUYING))
                .andThen(buy(paymentTransaction))))
        .doOnError(Throwable::printStackTrace)
        .retry()
        .subscribe();
  }

  private Completable buy(PaymentTransaction paymentTransaction) {
    return sendTransactionInteract.buy(paymentTransaction.getTransactionBuilder())
        .flatMapCompletable(hash -> pendingTransactionService.checkTransactionState(hash)
            .flatMapCompletable(pendingTransaction -> saveTransaction(pendingTransaction,
                paymentTransaction).onErrorResumeNext(
                throwable -> saveError(paymentTransaction, throwable))))
        .onErrorResumeNext(throwable -> saveError(paymentTransaction, throwable));
  }

  private CompletableSource saveError(PaymentTransaction paymentTransaction, Throwable throwable) {
    throwable.printStackTrace();
    return cache.save(paymentTransaction.getUri(),
        new PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.ERROR));
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
    return cache.getAll()
        .flatMapSingle(paymentTransactions -> Observable.fromIterable(paymentTransactions)
            .filter(paymentTransaction -> !paymentTransaction.getState()
                .equals(PaymentTransaction.PaymentState.APPROVED))
            .toList());
  }

  public Completable remove(String key) {
    return cache.remove(key);
  }
}
