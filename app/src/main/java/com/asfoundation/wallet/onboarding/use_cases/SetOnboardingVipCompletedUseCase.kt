package com.asfoundation.wallet.onboarding.use_cases

import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import javax.inject.Inject

class SetOnboardingVipCompletedUseCase
@Inject
constructor(private val commonsPreferencesDataSource: CommonsPreferencesDataSource) {
  operator fun invoke(walletAddress: String) =
    commonsPreferencesDataSource.setVipOnboardingToSeen(walletAddress)
}
