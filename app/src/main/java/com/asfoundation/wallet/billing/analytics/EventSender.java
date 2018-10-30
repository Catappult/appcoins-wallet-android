package com.asfoundation.wallet.billing.analytics;


public interface EventSender {

    void sendPurchaseDetailsEvent(String packageName, String skuDetails, String value, String purchaseDetail);

    void sendCreditCardDetailsEvent(String packageName, String skuDetails, String value);

    void sendPaymentEvent(String packageName, String skuDetails, String value);
}