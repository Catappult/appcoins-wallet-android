package com.asfoundation.wallet.advertise

import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

class PoaAnalyticsController @Inject constructor() {

  private val poaStartedEventList: MutableList<String> = CopyOnWriteArrayList()

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