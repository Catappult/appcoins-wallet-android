package com.asfoundation.wallet.billing.analytics;

interface PoaEventSender {

  void sendPoaStartedEvent(String packageName, String campaignId, String networkId);

  void sendPoaCompletedEvent(String packageName, String campaignId, String networkId);
}