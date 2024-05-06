package com.asfoundation.wallet.onboarding.use_cases

import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import javax.inject.Inject

class ShouldShowOnboardVipUseCase
@Inject
constructor(private val commonsPreferencesDataSource: CommonsPreferencesDataSource) {

  operator fun invoke(isVipLevel: Boolean, walletAddress: String): Boolean =
    isVipLevel && commonsPreferencesDataSource.firstVipOnboarding(walletAddress)
}
