package com.asfoundation.wallet.onboarding

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class OnboardingModule {

  @Provides
  fun providesOnboardingViewModelFactory() = OnboardingViewModelFactory()

  @Provides
  fun providesOnboardingNavigator(fragment: Fragment): OnboardingNavigator {
    return OnboardingNavigator(fragment)
  }

}