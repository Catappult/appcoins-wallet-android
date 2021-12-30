package com.asfoundation.wallet.onboarding.bottom_sheet

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class TermsConditionsBottomSheetModule {

  @Provides
  fun providesTermsConditionsBottomSheetViewModelFactory(
      setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase): TermsConditionsBottomSheetViewModelFactory {
    return TermsConditionsBottomSheetViewModelFactory(setOnboardingCompletedUseCase)
  }

  @Provides
  fun providesTermsConditionsBottomSheetNavigator(
      fragment: Fragment): TermsConditionsBottomSheetNavigator {
    return TermsConditionsBottomSheetNavigator(fragment)
  }
}