package com.asfoundation.wallet.billing.adyen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum PaymentType {
  CARD(Arrays.asList("visa", "mastercard", "card", "credit_card")),

  PAYPAL(Collections.singletonList("paypal")),

  PAYPALV2(Collections.singletonList("paypal_v2")),

  CHALLENGE_REWARD(Collections.singletonList("challenge_reward")),

  VKPAY(Collections.singletonList("vk_pay")),

  LOCAL_PAYMENTS(Collections.singletonList("localPayments")),

  CARRIER_BILLING(Collections.singletonList("carrier_billing")),

  SANDBOX(Collections.singletonList("sandbox")),

  GOOGLEPAY_WEB(Collections.singletonList("googlepay")),

  MI_PAY(Collections.singletonList("mipay")),

  TRUE_LAYER(Collections.singletonList("truelayer")),

  AMAZONPAY(Collections.singletonList("amazonpay"));

  private final List<String> subTypes;

  PaymentType(List<String> subTypes) {
    this.subTypes = subTypes;
  }

  public List<String> getSubTypes() {
    return subTypes;
  }
}
