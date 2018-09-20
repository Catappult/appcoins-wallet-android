package com.asfoundation.wallet.repository;

import io.reactivex.Completable;

public class NoValidateTransactionValidator implements TransactionValidator {

  public NoValidateTransactionValidator() {
  }

  @Override public Completable validate(PaymentTransaction paymentTransaction) {
    return Completable.complete();
  }
}
