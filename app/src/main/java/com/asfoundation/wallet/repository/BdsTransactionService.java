package com.asfoundation.wallet.repository;

import com.appcoins.wallet.commons.Repository;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

public class BdsTransactionService {
  private final Scheduler scheduler;
  private final Repository<String, BdsTransaction> cache;
  private final CompositeDisposable disposables;
  private final BdsPendingTransactionService transactionService;

  public BdsTransactionService(Scheduler scheduler, Repository<String, BdsTransaction> cache,
      CompositeDisposable disposables, BdsPendingTransactionService transactionService) {
    this.scheduler = scheduler;
    this.cache = cache;
    this.disposables = disposables;
    this.transactionService = transactionService;
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
            .andThen(
                transactionService.checkTransactionStateFromTransactionId(bdsTransaction.getUid())
                    .flatMapCompletable(pendingTransaction -> cache.save(bdsTransaction.getKey(),
                        new BdsTransaction(bdsTransaction,
                            pendingTransaction.isPending() ? BdsTransaction.Status.PROCESSING
                                : BdsTransaction.Status.COMPLETED)))))
        .doOnError(Throwable::printStackTrace)
        .retry()
        .subscribe());
  }

  public void stop() {
    disposables.clear();
  }

  public Completable trackTransaction(String key, String packageName, String skuId, String uid,
      String orderReference) {
    return cache.save(key, new BdsTransaction(uid, key, packageName, skuId, orderReference));
  }

  public Completable remove(String uri) {
    return cache.remove(uri);
  }

  public static class BdsTransaction {
    private final String key;
    private final String skuId;
    private final String packageName;
    private final Status status;
    private final String uid;
    private final String orderReference;

    public BdsTransaction(String uid, String key, String packageName, String skuId,
        String orderReference) {
      this.uid = uid;
      this.key = key;
      this.packageName = packageName;
      this.skuId = skuId;
      this.orderReference = orderReference;
      this.status = Status.WAITING;
    }

    public BdsTransaction(BdsTransaction transaction, Status status) {
      this.key = transaction.getKey();
      this.skuId = transaction.getSkuId();
      this.packageName = transaction.getPackageName();
      this.uid = transaction.getUid();
      this.status = status;
      this.orderReference = transaction.orderReference;
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

    public String getOrderReference() {
      return orderReference;
    }

    public String getSkuId() {
      return skuId;
    }

    public Status getStatus() {
      return status;
    }

    public String getUid() {
      return uid;
    }

    public enum Status {
      WAITING, PROCESSING, UNKNOWN_STATUS, COMPLETED
    }
  }
}
