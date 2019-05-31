package com.asfoundation.wallet.ui

import com.asfoundation.wallet.ui.iab.FiatValue

interface BalanceFragmentView {

  fun setupUI()

  fun updateTokenValue(tokenBalance: Balance)

  fun updateOverallBalance(overallBalance: FiatValue)
}
