package com.asfoundation.wallet.repository;

import com.appcoins.wallet.billing.BillingFactory;
import com.appcoins.wallet.billing.BillingPaymentProofSubmission;
import com.appcoins.wallet.billing.repository.entity.Transaction;
import com.asfoundation.wallet.entity.PendingTransaction;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import java.util.concurrent.TimeUnit;

public class BdsPendingTransactionService implements TrackTransactionService {
  private final BillingFactory billingFactory;
  private final Scheduler scheduler;
  private final long period;
  private final BillingPaymentProofSubmission billingPaymentProofSubmission;

  public BdsPendingTransactionService(BillingFactory billingFactory, Scheduler scheduler,
      long period, BillingPaymentProofSubmission billingPaymentProofSubmission) {
    this.billingFactory = billingFactory;
    this.scheduler = scheduler;
    this.period = period;
    this.billingPaymentProofSubmission = billingPaymentProofSubmission;
  }

  @Override public Observable<PendingTransaction> checkTransactionState(String hash) {
    return checkTransactionStateFromTransactionId(
        billingPaymentProofSubmission.getTransactionId(hash));
  }

  @Override public Observable<PendingTransaction> checkTransactionState(String hash, int chainId) {
    return Observable.error(new UnsupportedOperationException("Chain id no supported by bds flow"));
  }

  public Observable<PendingTransaction> checkTransactionStateFromTransactionId(String uid) {
    return Observable.interval(period, TimeUnit.SECONDS, scheduler)
        .timeInterval()
        .switchMap(scan -> billingFactory.getBilling("")
            .getAppcoinsTransaction(uid, scheduler)
            .map(transaction -> new PendingTransaction(transaction.getUid(),
                transaction.getStatus() == Transaction.Status.PROCESSING))
            .toObservable())
        .takeUntil(pendingTransaction -> !pendingTransaction.isPending());
  }
}
