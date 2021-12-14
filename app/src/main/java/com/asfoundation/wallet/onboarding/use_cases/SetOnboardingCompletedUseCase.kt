package com.asfoundation.wallet.onboarding.use_cases

import com.asfoundation.wallet.repository.PreferencesRepositoryType

class SetOnboardingCompletedUseCase(
    private val preferencesRepositoryType: PreferencesRepositoryType) {

  operator fun invoke() {
    preferencesRepositoryType.setOnboardingComplete()
  }
}