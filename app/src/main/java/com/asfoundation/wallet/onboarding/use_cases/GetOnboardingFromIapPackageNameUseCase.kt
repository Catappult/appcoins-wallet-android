package com.asfoundation.wallet.onboarding.use_cases

import com.asfoundation.wallet.repository.PreferencesRepositoryType
import javax.inject.Inject

class GetOnboardingFromIapPackageNameUseCase @Inject constructor(
  private val preferencesRepositoryType: PreferencesRepositoryType
) {

  operator fun invoke(): String? = preferencesRepositoryType.getOnboardingFromIapPackageName()

}