package com.asfoundation.wallet.onboarding.use_cases

import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import javax.inject.Inject

class SetOnboardingVipVisualisationStateUseCase
@Inject
constructor(private val commonsPreferencesDataSource: CommonsPreferencesDataSource) {
  operator fun invoke(walletAddress: String, hasSeen: Boolean) =
    commonsPreferencesDataSource.setVipOnboardingVisualisationState(walletAddress, hasSeen)
}
