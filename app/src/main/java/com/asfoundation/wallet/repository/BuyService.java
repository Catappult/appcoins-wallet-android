package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.interact.SendTransactionInteract;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import java.math.BigInteger;
import java.util.List;

/**
 * Created by trinkes on 3/16/18.
 */

public class BuyService {
  private final SendTransactionInteract sendTransactionInteract;
  private final Cache<String, PaymentTransaction> cache;
  private final ErrorMapper errorMapper;
  private final Scheduler scheduler;

  public BuyService(SendTransactionInteract sendTransactionInteract,
      Cache<String, PaymentTransaction> cache, ErrorMapper errorMapper, Scheduler scheduler) {
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
                .equals(PaymentTransaction.PaymentState.APPROVED))
            .flatMapCompletable(paymentTransaction -> cache.save(paymentTransaction.getUri(),
                new PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.BUYING))
                .andThen(buy(paymentTransaction))))
        .doOnError(Throwable::printStackTrace)
        .retry()
        .subscribe();
  }

  private Completable buy(PaymentTransaction paymentTransaction) {
    return sendTransactionInteract.buy(paymentTransaction.getTransactionBuilder(),
        paymentTransaction.getNonce()
            .add(BigInteger.ONE))
        .subscribeOn(scheduler)
        .flatMapCompletable(hash -> saveTransaction(hash, paymentTransaction))
        .onErrorResumeNext(throwable -> saveError(paymentTransaction, throwable));
  }

  private CompletableSource saveError(PaymentTransaction paymentTransaction, Throwable throwable) {
    throwable.printStackTrace();
    return cache.save(paymentTransaction.getUri(),
        new PaymentTransaction(paymentTransaction, errorMapper.map(throwable)));
  }

  private Completable saveTransaction(String hash, PaymentTransaction paymentTransaction) {
    return cache.save(paymentTransaction.getUri(),
        new PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.BOUGHT,
            paymentTransaction.getApproveHash(), hash));
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
