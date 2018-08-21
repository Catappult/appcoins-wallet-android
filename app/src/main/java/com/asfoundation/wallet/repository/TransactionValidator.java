package com.asfoundation.wallet.repository;

import io.reactivex.Completable;

public interface TransactionValidator {
  Completable validate(PaymentTransaction paymentTransaction);
}
