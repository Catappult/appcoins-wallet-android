package com.asfoundation.wallet.wallet_validation.dialog

import com.asfoundation.wallet.wallet_validation.ValidationInfo

interface WalletValidationDialogView {

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