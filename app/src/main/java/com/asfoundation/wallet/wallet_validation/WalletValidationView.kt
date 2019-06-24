package com.asfoundation.wallet.wallet_validation

import android.os.Bundle

interface WalletValidationView {

  fun finish()

  fun showError()

  fun close(bundle: Bundle?)

  fun showPhoneValidationView(countryCode: String?, phoneNumber: String?, errorMessage: Int? = null)

  fun showCodeValidationView(countryCode: String, phoneNumber: String)

  fun showCodeValidationView(validationInfo: ValidationInfo, errorMessage: Int)

  fun showLoading(it: ValidationInfo)

  fun showSuccess()

}