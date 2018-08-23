package com.asfoundation.wallet.repository;

import com.appcoins.wallet.billing.repository.entity.Transaction;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import java.util.concurrent.TimeUnit;

public class BdsPendingTransactionService {
  private final long period;
  private final Scheduler scheduler;
  private final Repository<String, BdsTransaction> cache;
  private final CompositeDisposable disposables;
  private final BdsTransactionProvider transactionProvider;

  public BdsPendingTransactionService(long period, Scheduler scheduler,
      Repository<String, BdsTransaction> cache, CompositeDisposable disposables,
      BdsTransactionProvider transactionProvider) {
    this.period = period;
    this.scheduler = scheduler;
    this.cache = cache;
    this.disposables = disposables;
    this.transactionProvider = transactionProvider;
  }

  private Observable<BdsTransaction> checkTransactionState(BdsTransaction bdsTransaction) {
    return Observable.interval(period, TimeUnit.SECONDS, scheduler)
        .timeInterval()
        .switchMap(scan -> transactionProvider.get(bdsTransaction.getPackageName(),
            bdsTransaction.getSkuId())
            .map(transaction -> map(transaction, bdsTransaction))
            .toObservable())
        .takeUntil(pendingTransaction -> !pendingTransaction.getStatus()
            .equals(BdsTransaction.Status.PROCESSING));
  }

  private BdsTransaction map(Transaction transaction, BdsTransaction bdsTransaction) {
    return new BdsTransaction(bdsTransaction, mapTransactionStatus(transaction.getStatus()));
  }

  private BdsTransaction.Status mapTransactionStatus(Transaction.Status status) {
    switch (status) {
      default:
      case PENDING:
      case FAILED:
      case INVALID_TRANSACTION:
      case CANCELED:
      case PENDING_SERVICE_AUTHORIZATION:
        return BdsTransaction.Status.UNKNOWN_STATUS;
      case PROCESSING:
        return BdsTransaction.Status.PROCESSING;
      case COMPLETED:
        return BdsTransaction.Status.COMPLETED;
    }
  }

  public Observable<BdsTransaction> getTransaction(String key) {
    return cache.get(key)
        .filter(transaction -> !transaction.getStatus()
            .equals(BdsTransaction.Status.WAITING));
  }

  public void start() {
    disposables.add(cache.getAll()
        .subscribeOn(scheduler)
        .flatMapIterable(bdsTransactions -> bdsTransactions)
        .filter(bdsTransaction -> bdsTransaction.getStatus()
            .equals(BdsTransaction.Status.WAITING))
        .flatMapCompletable(bdsTransaction -> cache.save(bdsTransaction.getKey(),
            new BdsTransaction(bdsTransaction, BdsTransaction.Status.PROCESSING))
            .andThen(checkTransactionState(bdsTransaction).flatMapCompletable(
                pendingTransaction -> cache.save(pendingTransaction.getKey(), pendingTransaction))))
        .doOnError(Throwable::printStackTrace)
        .retry()
        .subscribe());
  }

  public void stop() {
    disposables.clear();
  }

  public Completable trackTransaction(String key, String packageName, String skuId) {
    return cache.save(key, new BdsTransaction(key, packageName, skuId));
  }

  public Completable remove(String uri) {
    return cache.remove(uri);
  }

  public static class BdsTransaction {
    private final String key;
    private final String skuId;
    private final String packageName;
    private final Status status;

    public BdsTransaction(String key, String packageName, String skuId) {
      this.key = key;
      this.packageName = packageName;
      this.skuId = skuId;
      this.status = Status.WAITING;
    }

    public BdsTransaction(BdsTransaction transaction, Status status) {
      this.key = transaction.getKey();
      this.skuId = transaction.getSkuId();
      this.packageName = transaction.getPackageName();
      this.status = status;
    }

    @Override public int hashCode() {
      int result = key.hashCode();
      result = 31 * result + skuId.hashCode();
      result = 31 * result + packageName.hashCode();
      result = 31 * result + status.hashCode();
      return result;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof BdsTransaction)) return false;

      BdsTransaction that = (BdsTransaction) o;

      if (!key.equals(that.key)) return false;
      if (!skuId.equals(that.skuId)) return false;
      if (!packageName.equals(that.packageName)) return false;
      return status == that.status;
    }

    @Override public String toString() {
      return "BdsTransaction{"
          + "key='"
          + key
          + '\''
          + ", skuId='"
          + skuId
          + '\''
          + ", packageName='"
          + packageName
          + '\''
          + ", status="
          + status
          + '}';
    }

    public String getKey() {
      return key;
    }

    public String getPackageName() {
      return packageName;
    }

    public String getSkuId() {
      return skuId;
    }

    public Status getStatus() {
      return status;
    }

    public enum Status {
      WAITING, PROCESSING, UNKNOWN_STATUS, COMPLETED
    }
  }
}
