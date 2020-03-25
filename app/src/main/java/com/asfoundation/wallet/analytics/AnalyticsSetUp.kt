package com.asfoundation.wallet.analytics

interface AnalyticsSetUp {

  fun setUserId(walletAddress: String)

  fun setGamificationLevel(level: Int)
}