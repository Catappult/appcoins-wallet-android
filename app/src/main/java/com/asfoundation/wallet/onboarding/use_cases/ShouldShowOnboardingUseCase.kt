package com.asfoundation.wallet.onboarding.use_cases

import repository.CommonsPreferencesDataSource
import javax.inject.Inject

class ShouldShowOnboardingUseCase @Inject constructor(
  private val commonsPreferencesDataSource: CommonsPreferencesDataSource
) {

  operator fun invoke(): Boolean = !commonsPreferencesDataSource.hasCompletedOnboarding()

}