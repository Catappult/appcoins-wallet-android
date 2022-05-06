package com.asfoundation.wallet.verification.ui.paypal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.verification.usecases.GetVerificationInfoUseCase
import com.asfoundation.wallet.verification.usecases.MakeVerificationPaymentUseCase
import com.asfoundation.wallet.verification.usecases.SetCachedVerificationUseCase

class VerificationPaypalViewModelFactory(private val data: VerificationPaypalData,
                                         private val getVerificationInfoUseCase: GetVerificationInfoUseCase,
                                         private val makeVerificationPaymentUseCase: MakeVerificationPaymentUseCase,
                                         private val setCachedVerificationUseCase: SetCachedVerificationUseCase) : ViewModelProvider.Factory {

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return VerificationPaypalViewModel(data, getVerificationInfoUseCase,
        makeVerificationPaymentUseCase, setCachedVerificationUseCase) as T
  }
}