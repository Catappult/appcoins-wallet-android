package com.asfoundation.wallet.billing;

import com.adyen.core.models.Payment;
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization;
import io.reactivex.Completable;
import io.reactivex.Observable;

public interface CreditCardBilling {

  Observable<AdyenAuthorization> getAuthorization(String productName, String developerAddress, String payload);

  Completable authorize(Payment payment, String paykey);

  String getTransactionUid();
}