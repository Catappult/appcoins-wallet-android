package com.asfoundation.wallet.ui.onboarding

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.interact.CreateWalletInteract
import com.asfoundation.wallet.repository.PreferenceRepositoryType
import io.reactivex.Single

class OnboardingInteract(
    private val walletInteract: CreateWalletInteract,
    private val walletService: WalletService,
    private val preferenceRepositoryType: PreferenceRepositoryType) {

  fun getWalletAddress(): Single<String> {
    return walletService.getWalletAddress()
  }

  fun createWallet(): Single<String> {
    return walletInteract.create()
        .map { it.address }
  }

  fun finishOnboarding() {
    preferenceRepositoryType.setOnboardingComplete()
  }

  fun clickSkipOnboarding() {
    preferenceRepositoryType.setOnboardingSkipClicked()
  }

  fun hasClickedSkipOnboarding(): Boolean {
    return preferenceRepositoryType.hasClickedSkipOnboarding()
  }
}
