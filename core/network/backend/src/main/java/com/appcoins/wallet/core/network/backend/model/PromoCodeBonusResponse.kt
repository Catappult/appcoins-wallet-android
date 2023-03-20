package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName

data class PromoCodeBonusResponse(val code: String, val bonus: Double? = null, val app: App) {

  data class App(
    @SerializedName("package_name") val packageName: String?,
    @SerializedName("app_name") val appName: String?,
    @SerializedName("app_icon") val appIcon: String?
  )
}