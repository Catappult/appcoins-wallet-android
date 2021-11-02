package com.asfoundation.wallet.verification.paypal.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.verification.credit_card.WalletVerificationInteractor
import com.asfoundation.wallet.verification.usecases.GetVerificationInfoUseCase

class VerificationPaypalIntroViewModelFactory(
    private val data: VerificationPaypalIntroData,
    private val getVerificationInfoUseCase: GetVerificationInfoUseCase,
    private val walletVerificationInteractor: WalletVerificationInteractor
) : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return VerificationPaypalIntroViewModel(data, getVerificationInfoUseCase,
        walletVerificationInteractor) as T
  }
}