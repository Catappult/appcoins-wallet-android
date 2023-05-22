package com.asfoundation.wallet.repository;

import com.appcoins.wallet.core.network.microservices.model.Transaction;
import io.reactivex.Single;

public class NoValidateTransactionValidator implements TransactionValidator {

  public NoValidateTransactionValidator() {
  }

  @Override public Single<Transaction> validate(PaymentTransaction paymentTransaction) {
    return Single.just(Transaction.Companion.notFound());
  }
}
