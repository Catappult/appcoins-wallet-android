package com.asfoundation.wallet.advertise

class PoaAnalyticsController(private val poaStartedEventList: MutableList<String>) {

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