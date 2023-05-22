package com.appcoins.wallet.core.analytics.analytics

interface AnalyticsSetup {

  fun setUserId(walletAddress: String)

  fun setGamificationLevel(level: Int)

  fun setWalletOrigin(origin: String)

  fun setPromoCode(code: String?, bonus: Double?, validity: Int?, appName: String?)
}