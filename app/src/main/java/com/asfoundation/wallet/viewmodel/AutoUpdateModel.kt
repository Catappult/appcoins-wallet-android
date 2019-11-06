package com.asfoundation.wallet.viewmodel

import java.util.*

data class AutoUpdateModel(val updateVersionCode: Int = -1, val updateMinSdk: Int = -1,
                           val blackList: List<Int> = Collections.emptyList()) {

  fun isValid(): Boolean {
    return updateVersionCode != -1
  }
}
