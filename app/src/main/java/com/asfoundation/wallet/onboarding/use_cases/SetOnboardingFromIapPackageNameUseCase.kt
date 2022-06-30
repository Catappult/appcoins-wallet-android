package com.asfoundation.wallet.onboarding.use_cases

import com.asfoundation.wallet.repository.PreferencesRepositoryType
import javax.inject.Inject

class SetOnboardingFromIapPackageNameUseCase @Inject constructor(
  private val preferencesRepositoryType: PreferencesRepositoryType
) {

  operator fun invoke(appPackageName: String) {
    preferencesRepositoryType.setOnboardingFromIapPackageName(appPackageName)
  }
}