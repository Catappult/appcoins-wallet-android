package com.asfoundation.wallet.onboarding

import dagger.Module
import dagger.Provides

@Module
class OnboardingModule {

  @Provides
  fun providesOnboardingViewModelFactory() = OnboardingViewModelFactory()

  @Provides
  fun providesOnboardingNavigator(fragment: OnboardingFragment): OnboardingNavigator {
    return OnboardingNavigator(fragment)
  }

}