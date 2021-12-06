package com.asfoundation.wallet.onboarding.bottom_sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.onboarding.use_cases.GetWalletOrCreateUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase

class TermsConditionsBottomSheetViewModelFactory(
    private val getWalletOrCreateUseCase: GetWalletOrCreateUseCase,
    private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
    private val rxSchedulers: RxSchedulers) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return TermsConditionsBottomSheetViewModel(getWalletOrCreateUseCase,
        setOnboardingCompletedUseCase, rxSchedulers) as T
  }
}