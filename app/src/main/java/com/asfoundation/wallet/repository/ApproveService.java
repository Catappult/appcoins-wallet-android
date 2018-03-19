package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

/**
 * Created by trinkes on 3/16/18.
 */

public class ApproveService {
  private final SendTransactionInteract sendTransactionInteract;
  private final PendingTransactionService pendingTransactionService;
  private final Cache<String, PaymentTransaction> cache;

  public ApproveService(SendTransactionInteract sendTransactionInteract,
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
                .equals(PaymentTransaction.PaymentState.PENDING))
            .flatMapCompletable(this::approveTransaction))
        .doOnError(Throwable::printStackTrace)
        .retry()
        .subscribe();
  }

  private Completable approveTransaction(PaymentTransaction paymentTransaction) {
    return cache.save(paymentTransaction.getUri(),
        new PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.APPROVING))
        .andThen(sendTransactionInteract.approve(paymentTransaction.getTransactionBuilder())
            .flatMapCompletable(hash -> pendingTransactionService.checkTransactionState(hash)
                .flatMapCompletable(pendingTransaction -> saveTransaction(pendingTransaction,
                    paymentTransaction).onErrorResumeNext(throwable -> {
                  throwable.printStackTrace();
                  return cache.save(paymentTransaction.getUri(),
                      new PaymentTransaction(paymentTransaction,
                          PaymentTransaction.PaymentState.ERROR));
                }))))
        .onErrorResumeNext(throwable -> cache.save(paymentTransaction.getUri(),
            new PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.ERROR)));
  }

  private Completable saveTransaction(PendingTransaction pendingTransaction,
      PaymentTransaction paymentTransaction) {
    if (pendingTransaction.isPending()) {
      return cache.save(paymentTransaction.getUri(),
          new PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.APPROVING,
              pendingTransaction.getHash()));
    }
    return cache.save(paymentTransaction.getUri(),
        new PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.APPROVED,
            pendingTransaction.getHash()));
  }

  public Completable approve(String key, PaymentTransaction paymentTransaction) {
    return cache.save(key,
        new PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.PENDING));
  }

  public Observable<PaymentTransaction> getApprove(String uri) {
    return cache.get(uri);
  }

  public Observable<List<PaymentTransaction>> getAll() {
    return cache.getAll()
        .flatMapSingle(paymentTransactions -> Observable.fromIterable(paymentTransactions)
            .filter(paymentTransaction -> !paymentTransaction.getState()
                .equals(PaymentTransaction.PaymentState.PENDING))
            .toList());
  }

  public Completable remove(String key) {
    return cache.remove(key);
  }
}
