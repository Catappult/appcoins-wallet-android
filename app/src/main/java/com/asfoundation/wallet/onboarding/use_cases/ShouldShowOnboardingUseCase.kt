package com.asfoundation.wallet.onboarding.use_cases

import repository.PreferencesRepositoryType
import javax.inject.Inject

class ShouldShowOnboardingUseCase @Inject constructor(
  private val preferencesRepositoryType: PreferencesRepositoryType
) {

  operator fun invoke(): Boolean = !preferencesRepositoryType.hasCompletedOnboarding()

}