package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.interact.SendTransactionInteract;
import io.reactivex.Completable;

public class NoValidateTransactionValidatorOnChain implements TransactionValidator {

  private final SendTransactionInteract sendTransactionInteract;
  private final TrackTransactionService pendingTransactionService;

  public NoValidateTransactionValidatorOnChain(SendTransactionInteract sendTransactionInteract,
      TrackTransactionService pendingTransactionService) {
    this.sendTransactionInteract = sendTransactionInteract;
    this.pendingTransactionService = pendingTransactionService;
  }

  @Override public Completable validate(PaymentTransaction paymentTransaction) {
    return Completable.complete();
  }
}
