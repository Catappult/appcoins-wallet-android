package com.asfoundation.wallet.billing.payment;

import io.reactivex.Single;

public class AdyenPaymentService extends PaymentService {

  private final Adyen adyen;

  public AdyenPaymentService(String id, String type, String name, String description, String icon,
      Adyen adyen) {
    super(id, type, name, description, icon);
    this.adyen = adyen;
  }

  public Single<String> getToken() {
    return adyen.createToken();
  }
}
