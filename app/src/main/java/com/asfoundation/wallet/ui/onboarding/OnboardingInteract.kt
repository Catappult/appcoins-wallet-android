package com.asfoundation.wallet.ui.onboarding

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.interact.CreateWalletInteract
import com.asfoundation.wallet.repository.PreferenceRepositoryType
import com.asfoundation.wallet.repository.TokenRepository
import com.asfoundation.wallet.repository.TokenRepositoryType
import io.reactivex.Single

class OnboardingInteract(private val walletInteract: CreateWalletInteract,
                         private val walletService: WalletService,
                         private val preferenceRepositoryType: PreferenceRepositoryType,
                         private val tokenRepository: TokenRepositoryType) {

  fun getWalletAddress(): Single<String> {
    return walletService.getWalletAddress()
  }

  fun createWallet(): Single<String> {
    return walletInteract.create().flatMap { wallet ->
      walletInteract.setDefaultWallet(wallet)
          .andThen(tokenRepository.fetchAll(wallet.address))
          .firstOrError().map { wallet.address }
    }
  }

  fun finishOnboarding() {
    preferenceRepositoryType.setOnboardingComplete()
  }
}
