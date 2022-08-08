package com.asfoundation.wallet.onboarding.use_cases

import com.asfoundation.wallet.repository.PreferencesRepositoryType
import javax.inject.Inject

class ShouldShowOnboardingUseCase @Inject constructor(
  private val preferencesRepositoryType: PreferencesRepositoryType
) {

  operator fun invoke(): Boolean = !preferencesRepositoryType.hasCompletedOnboarding()

}