package com.asfoundation.wallet.repository;

import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmission;
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction;
import com.asfoundation.wallet.entity.PendingTransaction;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import java.util.concurrent.TimeUnit;

public class BdsPendingTransactionService implements TrackTransactionService {
  private final Billing billing;
  private final Scheduler scheduler;
  private final long period;
  private final BillingPaymentProofSubmission billingPaymentProofSubmission;

  public BdsPendingTransactionService(Billing billing, Scheduler scheduler, long period,
      BillingPaymentProofSubmission billingPaymentProofSubmission) {
    this.billing = billing;
    this.scheduler = scheduler;
    this.period = period;
    this.billingPaymentProofSubmission = billingPaymentProofSubmission;
  }

  @Override public Observable<PendingTransaction> checkTransactionState(String hash) {
    return checkTransactionStateFromTransactionId(
        billingPaymentProofSubmission.getTransactionId(hash));
  }

  public Observable<PendingTransaction> checkTransactionStateFromTransactionId(String uid) {
    return Observable.interval(period, TimeUnit.SECONDS, scheduler)
        .timeInterval()
        .switchMap(scan -> billing.getAppcoinsTransaction(uid, scheduler)
            .map(transaction -> new PendingTransaction(transaction.getUid(),
                transaction.getStatus() == Transaction.Status.PROCESSING))
            .toObservable())
        .takeUntil(pendingTransaction -> !pendingTransaction.isPending());
  }
}
