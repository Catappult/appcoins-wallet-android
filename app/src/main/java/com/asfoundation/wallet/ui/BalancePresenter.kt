package com.asfoundation.wallet.ui

class BalancePresenter(private val view: BalanceActivityView) {

  fun present() {
    view.showBalanceScreen()
  }
}
