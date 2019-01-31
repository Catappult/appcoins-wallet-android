package com.asfoundation.wallet.advertise

class PoaAnalyticsController {

  @Volatile
  private var poaStartedEventList = mutableListOf<String>()


  fun wasStartedEventSent(packageName: String): Boolean {
    return poaStartedEventList.contains(packageName)
  }

  fun setStartedEventSentFor(packageName: String) {
    poaStartedEventList.add(packageName)
  }

  fun cleanStateFor(packageName: String) {
    poaStartedEventList.remove(packageName)
  }

}