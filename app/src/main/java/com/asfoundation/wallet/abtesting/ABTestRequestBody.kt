package com.asfoundation.wallet.abtesting

import java.util.*

class ABTestRequestBody(action: String?) {

  private val events: MutableList<Data>

  init {
    events = ArrayList()
    events.add(Data(action))
  }

  data class Data internal constructor(val name: String?)
}
