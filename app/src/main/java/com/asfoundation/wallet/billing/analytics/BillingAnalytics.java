package com.asfoundation.wallet.billing.analytics;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import cm.aptoide.analytics.AnalyticsManager;

public class BillingAnalytics implements EventSender {
    private static final String WALLET = "WALLET";
    public static final String PURCHASE_DETAILS = "PURCHASE_DETAILS";
    public static final String CREDIT_CARD_DETAILS = "CREDIT_CARD_DETAILS";
    public static final String PAYMENT = "PAYMENT";
    private final AnalyticsManager analytics;

    public BillingAnalytics(AnalyticsManager analytics) {
        this.analytics = analytics;
    }

    @Override
    public void sendPurchaseDetailsEvent(String packageName, String skuDetails, String value, String purchaseDetail) {
        Log.d("****", "****EVENT: PAYMENT WILL BE DONE USING " + purchaseDetail);

        Map<String, Object> map = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();

        map2.put("package_name", packageName);
        map2.put("sku", skuDetails);
        map2.put("value", value);
        Log.d("****PurchaseDetailEvent", "PackageName = " + packageName + "; skuDetails = " + skuDetails + "; Value: " + value);

        map.put("purchase", map2);
        map.put("payment_method", purchaseDetail);

        analytics.logEvent(map, PURCHASE_DETAILS, AnalyticsManager.Action.CLICK, WALLET);
    }

    @Override
    public void sendCreditCardDetailsEvent(String packageName, String skuDetails, String value) {
        Log.d("****", "****EVENT: CC DETAILS INTRODUCTION");

        Map<String, Object> map = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();

        map2.put("package_name", packageName);
        map2.put("sku", skuDetails);
        map2.put("value", value);
        Log.d("****CreditCardDetailsEv", "PackageName = " + packageName + "; skuDetails = " + skuDetails + "; Value: " + value);

        map.put("purchase", map2);

        analytics.logEvent(map, CREDIT_CARD_DETAILS, AnalyticsManager.Action.CLICK, WALLET);
    }

    @Override
    public void sendPaymentEvent(String packageName, String skuDetails, String value) {
        Log.d("****", "****EVENT: PURCHASE COMPLETE");

        Map<String, Object> map = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();

        map2.put("package_name", packageName);
        map2.put("sku", skuDetails);
        map2.put("value", value);
        Log.d("****sendPayment", "PackageName = " + packageName + "; skuDetails = " + skuDetails + "; Value: " + value);

        map.put("purchase", map2);

        analytics.logEvent(map, PAYMENT, AnalyticsManager.Action.IMPRESSION, WALLET);
    }
}