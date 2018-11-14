package com.asfoundation.wallet.billing.analytics;


public interface EventSender {

    void sendPurchaseDetailsEvent(String packageName, String skuDetails, String value, String purchaseDetail);

    void sendPaymentMethodDetailsEvent(String packageName, String skuDetails, String value, String purchaseDetails);

    void sendPaymentEvent(String packageName, String skuDetails, String value, String paymentDetails);
}