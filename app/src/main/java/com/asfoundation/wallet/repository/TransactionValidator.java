package com.asfoundation.wallet.repository;

import com.appcoins.wallet.core.network.microservices.model.Transaction;
import io.reactivex.Single;

public interface TransactionValidator {
  Single<Transaction> validate(PaymentTransaction paymentTransaction);
}
