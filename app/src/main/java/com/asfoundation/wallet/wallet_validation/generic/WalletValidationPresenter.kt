package com.asfoundation.wallet.wallet_validation.generic

class WalletValidationPresenter(private val view: WalletValidationView) {

  fun present(isSavedInstance: Boolean) {
    view.showPhoneValidationView(isSavedInstance = isSavedInstance)
  }

}