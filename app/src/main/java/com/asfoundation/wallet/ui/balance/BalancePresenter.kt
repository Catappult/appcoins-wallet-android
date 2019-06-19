package com.asfoundation.wallet.ui.balance

import android.os.Bundle

class BalancePresenter(private val view: BalanceActivityView) {

  fun present(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) {
      view.showBalanceScreen()
    }
  }
}
