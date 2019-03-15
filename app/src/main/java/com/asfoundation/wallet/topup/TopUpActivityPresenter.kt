package com.asfoundation.wallet.topup

class TopUpActivityPresenter(private val view: TopUpActivityView) {
  fun present() {
      view.showTopUpScreen()
  }
}
