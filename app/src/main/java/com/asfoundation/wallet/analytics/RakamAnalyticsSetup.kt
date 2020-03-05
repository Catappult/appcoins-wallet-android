package com.asfoundation.wallet.analytics

import io.rakam.api.Rakam

class RakamAnalyticsSetup : AnalyticsSetUp {

  private val rakamClient = Rakam.getInstance()

  override fun setDefaultWallet(walletAddress: String) {
    rakamClient.setUserId(walletAddress)
  }
}