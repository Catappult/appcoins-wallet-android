package com.asfoundation.wallet.verification.paypal.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.verification.usecases.GetVerificationInfoUseCase
import com.asfoundation.wallet.verification.usecases.MakeVerificationPaymentUseCase

class VerificationPaypalIntroViewModelFactory(
    private val data: VerificationPaypalIntroData,
    private val getVerificationInfoUseCase: GetVerificationInfoUseCase,
    private val makeVerificationPaymentUseCase: MakeVerificationPaymentUseCase
) : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return VerificationPaypalIntroViewModel(data, getVerificationInfoUseCase,
        makeVerificationPaymentUseCase) as T
  }
}