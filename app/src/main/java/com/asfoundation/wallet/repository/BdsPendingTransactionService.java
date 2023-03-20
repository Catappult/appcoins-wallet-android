package com.asfoundation.wallet.repository;

import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmission;
import com.appcoins.wallet.core.network.microservices.model.Transaction;
import com.appcoins.wallet.core.utils.android_common.RxSchedulers;
import com.asfoundation.wallet.entity.PendingTransaction;
import io.reactivex.Observable;
import it.czerwinski.android.hilt.annotations.BoundTo;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;

@BoundTo(supertype = TrackTransactionService.class) @Named("BdsPendingTransactionService")
public class BdsPendingTransactionService implements TrackTransactionService {
  private final Billing billing;
  private final RxSchedulers rxSchedulers;
  private final BillingPaymentProofSubmission billingPaymentProofSubmission;

  public @Inject BdsPendingTransactionService(Billing billing, RxSchedulers rxSchedulers,
      BillingPaymentProofSubmission billingPaymentProofSubmission) {
    this.billing = billing;
    this.rxSchedulers = rxSchedulers;
    this.billingPaymentProofSubmission = billingPaymentProofSubmission;
  }

  @Override public Observable<PendingTransaction> checkTransactionState(String hash) {
    return checkTransactionStateFromTransactionId(
        billingPaymentProofSubmission.getTransactionId(hash));
  }

  public Observable<PendingTransaction> checkTransactionStateFromTransactionId(String uid) {
    return Observable.interval(5, TimeUnit.SECONDS, rxSchedulers.getIo())
        .timeInterval()
        .switchMap(scan -> billing.getAppcoinsTransaction(uid, rxSchedulers.getIo())
            .map(transaction -> new PendingTransaction(transaction.getUid(),
                transaction.getStatus() == Transaction.Status.PROCESSING))
            .toObservable())
        .takeUntil(pendingTransaction -> !pendingTransaction.isPending());
  }
}
