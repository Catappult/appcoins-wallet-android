package com.asfoundation.wallet.billing.adyen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum PaymentType {
  CARD(Arrays.asList("visa", "mastercard", "card", "credit_card")), PAYPAL(
      Collections.singletonList("paypal")), LOCAL_PAYMENTS(
      Collections.singletonList("localPayments")), CARRIER_BILLING(
      Collections.singletonList("carrier_billing"));

  private final List<String> subTypes;

  PaymentType(List<String> subTypes) {
    this.subTypes = subTypes;
  }

  public List<String> getSubTypes() {
    return subTypes;
  }
}
