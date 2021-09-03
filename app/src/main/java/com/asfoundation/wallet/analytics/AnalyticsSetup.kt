package com.asfoundation.wallet.analytics

import com.appcoins.wallet.gamification.repository.entity.WalletOrigin

interface AnalyticsSetup {

  fun setUserId(walletAddress: String)

  fun setGamificationLevel(level: Int)

  fun setWalletOrigin(origin: WalletOrigin)
}