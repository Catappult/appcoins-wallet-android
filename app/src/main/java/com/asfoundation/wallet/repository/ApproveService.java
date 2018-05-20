package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.interact.SendTransactionInteract;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import java.util.List;

/**
 * Created by trinkes on 3/16/18.
 */

public class ApproveService {
  private final SendTransactionInteract sendTransactionInteract;
  private final Repository<String, PaymentTransaction> cache;
  private final ErrorMapper errorMapper;
  private final Scheduler scheduler;

  public ApproveService(SendTransactionInteract sendTransactionInteract,
      Repository<String, PaymentTransaction> cache, ErrorMapper errorMapper, Scheduler scheduler) {
    this.sendTransactionInteract = sendTransactionInteract;
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
        .observeOn(scheduler)
        .andThen(sendTransactionInteract.approve(paymentTransaction.getTransactionBuilder(),
            paymentTransaction.getNonce())
            .flatMapCompletable(hash -> saveTransaction(hash, paymentTransaction)))
        .onErrorResumeNext(throwable -> cache.save(paymentTransaction.getUri(),
            new PaymentTransaction(paymentTransaction, errorMapper.map(throwable))));
  }

  private Completable saveTransaction(String hash, PaymentTransaction paymentTransaction) {
    return cache.save(paymentTransaction.getUri(),
        new PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.APPROVED, hash));
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
