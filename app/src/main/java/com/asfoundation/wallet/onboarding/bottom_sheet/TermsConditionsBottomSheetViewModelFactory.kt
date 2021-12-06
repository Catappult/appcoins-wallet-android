package com.asfoundation.wallet.onboarding.bottom_sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase

class TermsConditionsBottomSheetViewModelFactory(
    private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return TermsConditionsBottomSheetViewModel(setOnboardingCompletedUseCase) as T
  }
}