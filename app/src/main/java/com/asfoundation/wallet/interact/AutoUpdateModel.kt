package com.asfoundation.wallet.interact

data class AutoUpdateModel(val versionCode: Int = -1, val redirectUrl: String = "",
                           val minSdk: Int = -1, val availableInAptoide: Boolean = false,
                           val availableInPlay: Boolean = false) {
  fun isValid(): Boolean {
    return versionCode != -1
  }
}
