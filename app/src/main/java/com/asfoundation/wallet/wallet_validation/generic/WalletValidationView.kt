package com.asfoundation.wallet.wallet_validation.generic

import com.asfoundation.wallet.wallet_validation.ValidationInfo

interface WalletValidationView {

  fun showPhoneValidationView(countryCode: String? = null, phoneNumber: String? = null,
                              errorMessage: Int? = null, isSavedInstance: Boolean = false)

  fun showCodeValidationView(countryCode: String, phoneNumber: String)

  fun showCodeValidationView(validationInfo: ValidationInfo, errorMessage: Int)

  fun finishSuccessActivity()

  fun showLastStepAnimation()

  fun showProgressAnimation()

  fun hideProgressAnimation()

  fun finishCancelActivity()
}