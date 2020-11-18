package com.asfoundation.wallet.ui.onboarding

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethodEntity
import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.support.SupportRepository
import io.reactivex.Single

class OnboardingInteractor(private val walletService: WalletService,
                           private val preferencesRepositoryType: PreferencesRepositoryType,
                           private val supportRepository: SupportRepository,
                           private val gamificationRepository: Gamification,
                           private val bdsRepository: BdsRepository) {

  fun getWalletAddress() = walletService.getWalletOrCreate()
      .flatMap { address ->
        gamificationRepository.getUserStats(address)
            .doOnSuccess { supportRepository.registerUser(it.level, address) }
            .map { address }
      }

  fun saveOnboardingCompleted() = preferencesRepositoryType.setOnboardingComplete()

  fun clickSkipOnboarding() = preferencesRepositoryType.setOnboardingSkipClicked()

  fun hasClickedSkipOnboarding() = preferencesRepositoryType.hasClickedSkipOnboarding()

  fun hasOnboardingCompleted() = preferencesRepositoryType.hasCompletedOnboarding()

  fun getPaymentMethodsIcons(): Single<List<String>> {
    return bdsRepository.getPaymentMethods(currencyType = "fiat", direct = true)
        .map { map(it) }
        .onErrorReturn { emptyList() }
  }

  private fun map(paymentMethodEntity: List<PaymentMethodEntity>): List<String> {
    return paymentMethodEntity.map { it.iconUrl }
  }
}
