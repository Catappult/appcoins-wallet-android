package com.asfoundation.wallet.onboarding.use_cases

import com.asfoundation.wallet.repository.PreferencesRepositoryType
import javax.inject.Inject

class SetOnboardingCompletedUseCase @Inject constructor(
    private val preferencesRepositoryType: PreferencesRepositoryType) {

  operator fun invoke() {
    preferencesRepositoryType.setOnboardingComplete()
  }
}