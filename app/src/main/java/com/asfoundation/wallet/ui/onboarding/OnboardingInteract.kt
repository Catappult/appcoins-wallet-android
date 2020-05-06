package com.asfoundation.wallet.ui.onboarding

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.interact.CreateWalletInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType

class OnboardingInteract(
    private val walletInteract: CreateWalletInteract,
    private val walletService: WalletService,
    private val preferencesRepositoryType: PreferencesRepositoryType) {

  fun getWalletAddress() = walletService.getWalletAddress()

  fun createWallet() = walletInteract.create()
      .map { it.address }

  fun finishOnboarding() {
    preferencesRepositoryType.setOnboardingComplete()
  }

  fun clickSkipOnboarding() {
    preferencesRepositoryType.setOnboardingSkipClicked()
  }

  fun hasClickedSkipOnboarding() = preferencesRepositoryType.hasClickedSkipOnboarding()

  fun hasOnboardingCompleted() = preferencesRepositoryType.hasCompletedOnboarding()
}
