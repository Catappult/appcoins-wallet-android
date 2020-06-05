package com.asfoundation.wallet.ui.onboarding

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.repository.PreferencesRepositoryType

class OnboardingInteract(
    private val walletService: WalletService,
    private val preferencesRepositoryType: PreferencesRepositoryType) {

  fun getWalletAddress() = walletService.getWalletOrCreate()

  fun finishOnboarding() {
    preferencesRepositoryType.setOnboardingComplete()
  }

  fun clickSkipOnboarding() {
    preferencesRepositoryType.setOnboardingSkipClicked()
  }

  fun hasClickedSkipOnboarding() = preferencesRepositoryType.hasClickedSkipOnboarding()

  fun hasOnboardingCompleted() = preferencesRepositoryType.hasCompletedOnboarding()
}
