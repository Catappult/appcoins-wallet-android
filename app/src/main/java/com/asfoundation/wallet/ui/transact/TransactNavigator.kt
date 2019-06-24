package com.asfoundation.wallet.ui.transact

import java.math.BigDecimal

interface TransactNavigator {
  fun openAppcoinsCreditsSuccess(walletAddress: String, amount: BigDecimal,
                                 currency: String)
  fun showLoading()
  fun hideLoading()
  fun closeScreen()
  fun hideKeyboard()
  fun openQrCodeScreen()
}
