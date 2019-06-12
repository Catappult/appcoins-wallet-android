package com.asfoundation.wallet.wallet_validation

class WalletValidationPresenter(
    private val view: WalletValidationActivity
) {

  fun present() {
    view.showPhoneValidationView(null, null)
  }
}