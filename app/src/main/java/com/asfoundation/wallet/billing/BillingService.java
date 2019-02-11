package com.asfoundation.wallet.billing;

import com.adyen.core.models.Payment;
import com.adyen.core.models.PaymentMethod;
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.math.BigDecimal;

public interface BillingService {

  Observable<AdyenAuthorization> getAuthorization(String productName, String developerAddress,
      String payload, String origin, BigDecimal priceValue, String priceCurrency, String type,
      String callback, String orderReference, String appPackageName);

  Completable authorize(Payment payment, String paykey);

  String getTransactionUid();

  Completable deletePaymentMethod(PaymentMethod paymentMethod, String skuId,
      String developerAddress, String developerPayload, String origin, BigDecimal bigDecimal,
      String currency, String type, String callbackUrl, String orderReference, String appPackage);
}