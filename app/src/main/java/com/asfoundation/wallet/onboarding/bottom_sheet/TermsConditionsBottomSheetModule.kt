package com.asfoundation.wallet.onboarding.bottom_sheet

import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import dagger.Module
import dagger.Provides

@Module
class TermsConditionsBottomSheetModule {

  @Provides
  fun providesTermsConditionsBottomSheetViewModelFactory(
      setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase): TermsConditionsBottomSheetViewModelFactory {
    return TermsConditionsBottomSheetViewModelFactory(setOnboardingCompletedUseCase)
  }

  @Provides
  fun providesTermsConditionsBottomSheetNavigator(
      fragment: TermsConditionsBottomSheetFragment): TermsConditionsBottomSheetNavigator {
    return TermsConditionsBottomSheetNavigator(fragment)
  }
}