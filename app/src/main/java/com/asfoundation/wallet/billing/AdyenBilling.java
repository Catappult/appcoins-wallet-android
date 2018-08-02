package com.asfoundation.wallet.billing;

import com.adyen.core.models.Payment;
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization;
import com.asfoundation.wallet.billing.payment.Adyen;
import com.jakewharton.rxrelay.BehaviorRelay;
import java.util.concurrent.atomic.AtomicBoolean;
import rx.Completable;
import rx.Observable;
import rx.Single;

public class AdyenBilling implements Billing {

  private final BehaviorRelay<AdyenAuthorization> relay;
  private final TransactionService transactionService;
  private final BillingSigner billingSigner;

  private final AtomicBoolean processingPayment;
  private final Adyen adyen;
  private AdyenAuthorization adyenAuthorization;
  private String transactionUid;

  public AdyenBilling(TransactionService transactionService, BillingSigner billingSigner,
      Adyen adyen) {
    this.adyen = adyen;
    this.relay = BehaviorRelay.create();
    this.transactionService = transactionService;
    this.billingSigner = billingSigner;

    this.processingPayment = new AtomicBoolean();
  }

  @Override public Observable<AdyenAuthorization> getAuthorization() {
    return relay.doOnSubscribe(this::startPaymentIfNeeded)
        .doOnNext(this::resetProcessingFlag);
  }

  @Override public Completable authorize(Payment payment, String paykey) {
    return Single.fromCallable(() -> payment.getPaymentStatus()
        .name()
        .equals("AUTHORISED"))
        .flatMapCompletable(
            authorized -> transactionService.finishTransaction(transactionUid, paykey)
                .andThen(Completable.fromAction(() -> callRelay(authorized))));
  }

  private void callRelay(boolean authorized) {
    if (authorized) {
      relay.call(new AdyenAuthorization(adyenAuthorization.getSession(),
          AdyenAuthorization.Status.REDEEMED));
    } else {
      relay.call(new AdyenAuthorization(adyenAuthorization.getSession(),
          AdyenAuthorization.Status.FAILED));
    }
  }

  private void resetProcessingFlag(AdyenAuthorization adyenAuthorization) {
    if (adyenAuthorization.isCompleted() || adyenAuthorization.isFailed()) {
      processingPayment.set(false);
    }
  }

  private void startPaymentIfNeeded() {
    // TODO: 31-07-2018 neuro recheck
    if (!processingPayment.getAndSet(true)) {
      this.adyenAuthorization = adyen.createToken()
          .flatMap(token -> transactionService.createTransaction(billingSigner.getAddress(),
              billingSigner.getSignature(), token))
          .doOnSuccess(transactionUid -> this.transactionUid = transactionUid)
          .flatMap(__ -> transactionService.getSession(transactionUid))
          .map(this::newDefaultAdyenAuthorization)
          .toBlocking()
          .value();

      relay.call(adyenAuthorization);
    }
  }

  private AdyenAuthorization newDefaultAdyenAuthorization(String session) {
    return new AdyenAuthorization(session, AdyenAuthorization.Status.PENDING);
  }
}
