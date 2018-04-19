package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by trinkes on 3/16/18.
 */

public class BuyService {
  private final SendTransactionInteract sendTransactionInteract;
  private final PendingTransactionService pendingTransactionService;
  private final Cache<String, PaymentTransaction> cache;
  private final ErrorMapper errorMapper;
  private final Scheduler scheduler;

  public BuyService(SendTransactionInteract sendTransactionInteract,
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
            .retryWhen(this::retryOnTransactionNotFound)
            .flatMapCompletable(pendingTransaction -> saveTransaction(pendingTransaction,
                paymentTransaction).onErrorResumeNext(
                throwable -> saveError(paymentTransaction, throwable))))
        .onErrorResumeNext(throwable -> saveError(paymentTransaction, throwable));
  }

  private Observable<Long> retryOnTransactionNotFound(Observable<Throwable> throwableObservable) {
    return throwableObservable.flatMap(throwable -> {
      if (throwable instanceof TransactionNotFoundException) {
        return Observable.timer(1, TimeUnit.SECONDS, Schedulers.trampoline());
      }
      return Observable.error(throwable);
    });
  }

  private CompletableSource saveError(PaymentTransaction paymentTransaction, Throwable throwable) {
    throwable.printStackTrace();
    return cache.save(paymentTransaction.getUri(),
        new PaymentTransaction(paymentTransaction, errorMapper.map(throwable)));
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
