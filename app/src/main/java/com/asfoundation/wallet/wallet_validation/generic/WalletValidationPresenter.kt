package com.asfoundation.wallet.wallet_validation.generic

class WalletValidationPresenter(private val view: WalletValidationView) {

  fun present() {
    view.showPhoneValidationView()
  }

}