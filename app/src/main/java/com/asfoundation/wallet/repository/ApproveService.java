package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by trinkes on 3/16/18.
 */

public class ApproveService {
  private final SendTransactionInteract sendTransactionInteract;
  private final PendingTransactionService pendingTransactionService;
  private final Cache<String, PaymentTransaction> cache;
  private final ErrorMapper errorMapper;
  private final Scheduler scheduler;

  public ApproveService(SendTransactionInteract sendTransactionInteract,
      PendingTransactionService pendingTransactionService, Cache<String, PaymentTransaction> cache,
      ErrorMapper errorMapper, Scheduler scheduler) {
    this.sendTransactionInteract = sendTransactionInteract;
    this.pendingTransactionService = pendingTransactionService;
    this.cache = cache;
    this.errorMapper = errorMapper;
    this.scheduler = scheduler;
  }

  public void start() {
    cache.getAll()
        .observeOn(scheduler)
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
                .retryWhen(this::retryOnTransactionNotFound)
                .flatMapCompletable(pendingTransaction -> saveTransaction(pendingTransaction,
                    paymentTransaction).onErrorResumeNext(throwable -> {
                  throwable.printStackTrace();
                  return cache.save(paymentTransaction.getUri(),
                      new PaymentTransaction(paymentTransaction, errorMapper.map(throwable)));
                }))))
        .onErrorResumeNext(throwable -> cache.save(paymentTransaction.getUri(),
            new PaymentTransaction(paymentTransaction, errorMapper.map(throwable))));
  }

  private Observable<Long> retryOnTransactionNotFound(Observable<Throwable> attempts) {
    return attempts.flatMap(throwable -> {
      if (throwable instanceof TransactionNotFoundException) {
        return Observable.timer(1, TimeUnit.SECONDS);
      }
      return Observable.error(throwable);
    });
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
