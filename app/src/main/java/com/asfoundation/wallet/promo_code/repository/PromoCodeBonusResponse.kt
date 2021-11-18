package com.asfoundation.wallet.promo_code.repository

data class PromoCodeBonusResponse(val code: String, val bonus: Double? = null, val app: App) {

  data class App(val packageName: String?, val appName: String?, val appIcon: String?)
}