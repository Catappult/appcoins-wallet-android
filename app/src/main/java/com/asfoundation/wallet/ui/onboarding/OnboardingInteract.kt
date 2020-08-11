package com.asfoundation.wallet.ui.onboarding

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethodEntity
import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.ReferralModel
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Single

class OnboardingInteract(
    private val walletService: WalletService,
    private val preferencesRepositoryType: PreferencesRepositoryType,
    private val supportInteractor: SupportInteractor,
    private val gamificationRepository: Gamification,
    private val smsValidationInteract: SmsValidationInteract,
    private val referralInteractor: ReferralInteractorContract,
    private val bdsRepository: BdsRepository) {

  fun getWalletAddress() = walletService.getWalletOrCreate()
      .flatMap { address ->
        gamificationRepository.getUserStats(address)
            .doOnSuccess { supportInteractor.registerUser(it.level, address) }
            .map { address }
      }

  fun finishOnboarding() = preferencesRepositoryType.setOnboardingComplete()

  fun clickSkipOnboarding() = preferencesRepositoryType.setOnboardingSkipClicked()

  fun hasClickedSkipOnboarding() = preferencesRepositoryType.hasClickedSkipOnboarding()

  fun hasOnboardingCompleted() = preferencesRepositoryType.hasCompletedOnboarding()

  fun isAddressValid(address: String): Single<WalletValidationStatus> =
      smsValidationInteract.getValidationStatus(address)

  fun getReferralInfo(): Single<ReferralModel> = referralInteractor.getReferralInfo()

  fun getPaymentMethodsIcons(): Single<List<String>> {
    return bdsRepository.getPaymentMethods(currencyType = "fiat", direct = true)
        .map { map(it) }
        .onErrorReturn { emptyList() }
  }

  private fun map(paymentMethodEntity: List<PaymentMethodEntity>): List<String> {
    return paymentMethodEntity.map { it.iconUrl }
  }
}
