package com.asfoundation.wallet.poa_wallet_validation

interface WalletValidationView {

  fun showPhoneValidationView(countryCode: String?, phoneNumber: String?, errorMessage: Int? = null)

  fun showCodeValidationView(countryCode: String, phoneNumber: String)

  fun showCodeValidationView(validationInfo: ValidationInfo, errorMessage: Int)

  fun showLoading(it: ValidationInfo)

  fun showSuccess()

  fun closeSuccess()

  fun closeCancel(removeTask: Boolean)

  fun closeError()

  fun showCreateAnimation()

  fun hideAnimation()
}