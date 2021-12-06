package com.asfoundation.wallet.onboarding

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.onboarding.OnboardingInteractor
import dagger.Module
import dagger.Provides

@Module
class OnboardingModule {

  @Provides
  fun providesOnboardingViewModelFactory() = OnboardingViewModelFactory()

  @Provides
  fun providesOnboardingNavigator(fragment: OnboardingFragment): OnboardingNavigator {
    return OnboardingNavigator(fragment)
  }

  @Provides
  fun providesOnboardingInteractor(walletService: WalletService,
                                   preferencesRepositoryType: PreferencesRepositoryType,
                                   supportInteractor: SupportInteractor, gamification: Gamification,
                                   bdsRepository: BdsRepository,
                                   getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase): OnboardingInteractor {
    return OnboardingInteractor(walletService, preferencesRepositoryType, supportInteractor,
        gamification, bdsRepository, getCurrentPromoCodeUseCase)
  }
}