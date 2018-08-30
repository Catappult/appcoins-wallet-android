package com.asfoundation.wallet.billing;

import com.adyen.core.models.Payment;
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization;
import rx.Completable;
import rx.Observable;

public interface CreditCardBilling {

  Observable<AdyenAuthorization> getAuthorization(String productName, String developerAddress, String payload);

  Completable authorize(Payment payment, String paykey);

  String getTransactionUid();
}