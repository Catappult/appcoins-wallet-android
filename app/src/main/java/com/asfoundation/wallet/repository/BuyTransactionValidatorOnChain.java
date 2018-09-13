package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.interact.SendTransactionInteract;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BuyTransactionValidatorOnChain implements TransactionValidator {

  private final SendTransactionInteract sendTransactionInteract;
  private final TrackTransactionService pendingTransactionService;

  public BuyTransactionValidatorOnChain(SendTransactionInteract sendTransactionInteract,
      TrackTransactionService pendingTransactionService) {
    this.sendTransactionInteract = sendTransactionInteract;
    this.pendingTransactionService = pendingTransactionService;
  }

  @Override public Completable validate(PaymentTransaction paymentTransaction) {
    return sendTransactionInteract.computeBuyTransactionHash(
        paymentTransaction.getTransactionBuilder())
        .flatMapObservable(pendingTransactionService::checkTransactionState)
        .retryWhen(errors -> {
          AtomicInteger counter = new AtomicInteger();
          return errors.takeWhile(e -> counter.getAndIncrement() != 3)
              .flatMap(e -> Observable.timer(counter.get(), TimeUnit.SECONDS));
        })
        .ignoreElements();
  }
}
