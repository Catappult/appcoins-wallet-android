package com.asfoundation.wallet.billing.analytics;


public interface EventSender {

    void sendPurchaseDetailsEvent(String packageName, String skuDetails, String value, String transactionType);

    void sendPaymentMethodDetailsEvent(String packageName, String skuDetails, String value, String purchaseDetails, String transactionType);

    void sendPaymentEvent(String packageName, String skuDetails, String value, String purchaseDetails, String transactionType);

    void sendRevenueEvent(String value);
}