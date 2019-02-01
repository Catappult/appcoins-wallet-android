package com.asfoundation.wallet.advertise

import java.util.concurrent.CopyOnWriteArrayList

class PoaAnalyticsController(private val poaStartedEventList: CopyOnWriteArrayList<String>) {

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