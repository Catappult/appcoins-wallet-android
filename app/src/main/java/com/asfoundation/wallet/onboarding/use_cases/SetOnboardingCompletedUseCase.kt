package com.asfoundation.wallet.onboarding.use_cases

import repository.PreferencesRepositoryType
import javax.inject.Inject

class SetOnboardingCompletedUseCase @Inject constructor(
    private val preferencesRepositoryType: PreferencesRepositoryType) {

  operator fun invoke() {
    preferencesRepositoryType.setOnboardingComplete()
  }
}