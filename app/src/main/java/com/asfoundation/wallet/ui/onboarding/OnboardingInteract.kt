package com.asfoundation.wallet.ui.onboarding

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.interact.CreateWalletInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import io.reactivex.Single

class OnboardingInteract(
    private val walletInteract: CreateWalletInteract,
    private val walletService: WalletService,
    private val preferencesRepositoryType: PreferencesRepositoryType) {

  fun getWalletAddress(): Single<String> {
    return walletService.getWalletAddress()
  }

  fun createWallet(): Single<String> {
    return walletInteract.create()
        .map { it.address }
  }

  fun finishOnboarding() {
    preferencesRepositoryType.setOnboardingComplete()
  }

  fun clickSkipOnboarding() {
    preferencesRepositoryType.setOnboardingSkipClicked()
  }

  fun hasClickedSkipOnboarding(): Boolean {
    return preferencesRepositoryType.hasClickedSkipOnboarding()
  }

  fun hasOnboardingCompleted(): Boolean {
    return preferencesRepositoryType.hasCompletedOnboarding()
  }
}
