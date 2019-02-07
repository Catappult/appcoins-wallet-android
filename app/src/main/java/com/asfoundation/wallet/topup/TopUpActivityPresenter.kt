package com.asfoundation.wallet.topup

class TopUpActivityPresenter(private val view: TopUpActivityView) {
  fun present(isCreating: Boolean) {
    if (isCreating) {
      view.showTopUpScreen()
    }
  }

}
