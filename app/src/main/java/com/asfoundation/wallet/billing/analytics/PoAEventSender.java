package com.asfoundation.wallet.billing.analytics;

interface PoAEventSender {

  void sendPoAStartedEvent(String packageName, String campaignId, String networkId);
}