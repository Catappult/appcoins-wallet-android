package com.asfoundation.wallet.ui.balance

import android.os.Bundle

class BalanceActivityPresenter(private val view: BalanceActivityView) {

  fun present(savedInstanceState: Bundle?) {
    savedInstanceState ?: view.showBalanceScreen()
  }
}
