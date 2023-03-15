package com.asfoundation.wallet.onboarding.use_cases

import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import javax.inject.Inject

class ShouldShowOnboardingUseCase @Inject constructor(
  private val commonsPreferencesDataSource: CommonsPreferencesDataSource
) {

  operator fun invoke(): Boolean = !commonsPreferencesDataSource.hasCompletedOnboarding()

}