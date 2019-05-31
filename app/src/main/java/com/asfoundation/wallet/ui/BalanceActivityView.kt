package com.asfoundation.wallet.ui

interface BalanceActivityView {

  fun showBalanceScreen()

  fun showTokenDetailsScreen(tokenId: String)

  fun showTopUpScreen()

  fun setupToolbar()

}
