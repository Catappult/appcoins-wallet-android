package com.asfoundation.wallet.analytics

import com.asfoundation.wallet.promotions.model.WalletOrigin


interface AnalyticsSetup {

  fun setUserId(walletAddress: String)

  fun setGamificationLevel(level: Int)

  fun setWalletOrigin(origin: WalletOrigin)
}