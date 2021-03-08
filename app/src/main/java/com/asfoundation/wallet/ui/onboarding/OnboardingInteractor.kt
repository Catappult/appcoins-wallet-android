package com.asfoundation.wallet.ui.onboarding

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethodEntity
import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.repository.ImpressionPreferencesRepositoryType
import com.asfoundation.wallet.support.SupportInteractor
import io.reactivex.Single

class OnboardingInteractor(private val walletService: WalletService,
                           private val impressionPreferencesRepositoryType: ImpressionPreferencesRepositoryType,
                           private val supportInteractor: SupportInteractor,
                           private val gamificationRepository: Gamification,
                           private val bdsRepository: BdsRepository) {

  fun getWalletAddress() = walletService.getWalletOrCreate()
      .flatMap { address ->
        gamificationRepository.getUserStats(address)
            .doOnSuccess { supportInteractor.registerUser(it.level, address) }
            .map { address }
      }

  fun saveOnboardingCompleted() = impressionPreferencesRepositoryType.setOnboardingComplete()

  fun clickSkipOnboarding() = impressionPreferencesRepositoryType.setOnboardingSkipClicked()

  fun hasClickedSkipOnboarding() = impressionPreferencesRepositoryType.hasClickedSkipOnboarding()

  fun hasOnboardingCompleted() = impressionPreferencesRepositoryType.hasCompletedOnboarding()

  fun getPaymentMethodsIcons(): Single<List<String>> {
    return bdsRepository.getPaymentMethods(currencyType = "fiat", direct = true)
        .map { map(it) }
        .onErrorReturn { emptyList() }
  }

  private fun map(paymentMethodEntity: List<PaymentMethodEntity>): List<String> {
    return paymentMethodEntity.map { it.iconUrl }
  }
}
