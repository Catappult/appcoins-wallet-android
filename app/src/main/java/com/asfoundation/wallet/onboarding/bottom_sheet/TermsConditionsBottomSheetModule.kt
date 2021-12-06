package com.asfoundation.wallet.onboarding.bottom_sheet

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.onboarding.use_cases.GetWalletOrCreateUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.onboarding.OnboardingInteractor
import dagger.Module
import dagger.Provides

@Module
class TermsConditionsBottomSheetModule {

  @Provides
  fun providesTermsConditionsBottomSheetViewModelFactory(
      getWalletOrCreateUseCase: GetWalletOrCreateUseCase,
      setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
      rxSchedulers: RxSchedulers): TermsConditionsBottomSheetViewModelFactory {
    return TermsConditionsBottomSheetViewModelFactory(getWalletOrCreateUseCase,
        setOnboardingCompletedUseCase, rxSchedulers)
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

  @Provides
  fun providesTermsConditionsBottomSheetNavigator(
      fragment: TermsConditionsBottomSheetFragment): TermsConditionsBottomSheetNavigator {
    return TermsConditionsBottomSheetNavigator(fragment)
  }
}