package com.asfoundation.wallet.repository;

import io.reactivex.Completable;

interface TransactionValidator {
  Completable validate(PaymentTransaction paymentTransaction);
}
