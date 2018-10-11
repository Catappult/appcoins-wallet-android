package com.asfoundation.wallet.billing;

import com.adyen.core.models.Payment;
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.math.BigDecimal;

public interface CreditCardBilling {

  Observable<AdyenAuthorization> getAuthorization(String productName, String developerAddress,
      String payload, String origin, BigDecimal priceValue, String priceCurrency, String type);

  Completable authorize(Payment payment, String paykey);

  String getTransactionUid();
}