package com.asfoundation.wallet.billing.analytics;

interface PoAEventSender {

  void sendPoaStartedEvent(String packageName, String campaignId, String networkId);
}