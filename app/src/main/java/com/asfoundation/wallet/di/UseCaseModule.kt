package com.asfoundation.wallet.di

import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.UserStatsLocalData
import com.asfoundation.wallet.change_currency.FiatCurrenciesRepository
import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.change_currency.use_cases.SetSelectedCurrencyUseCase
import com.asfoundation.wallet.gamification.ObserveLevelsUseCase
import com.asfoundation.wallet.promotions.model.PromotionsMapper
import com.asfoundation.wallet.promotions.usecases.GetPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenWalletOriginUseCase
import com.asfoundation.wallet.repository.WalletRepositoryType
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UseCaseModule {
  @Singleton
  @Provides
  fun providesGetPromotionsUseCase(getCurrentWallet: GetCurrentWalletUseCase,
                                   observeLevels: ObserveLevelsUseCase,
                                   promotionsMapper: PromotionsMapper,
                                   promotionsRepository: PromotionsRepository): GetPromotionsUseCase {
    return GetPromotionsUseCase(getCurrentWallet, observeLevels, promotionsMapper,
        promotionsRepository)
  }

  @Singleton
  @Provides
  fun providesGetCurrentWalletUseCase(
      walletRepository: WalletRepositoryType): GetCurrentWalletUseCase {
    return GetCurrentWalletUseCase(walletRepository)
  }

  @Singleton
  @Provides
  fun providesObserveLevelsUseCase(getCurrentWallet: GetCurrentWalletUseCase,
                                   gamification: Gamification): ObserveLevelsUseCase {
    return ObserveLevelsUseCase(getCurrentWallet, gamification)
  }

  @Singleton
  @Provides
  fun providesSetSeenWalletOriginUseCase(
      userStatsLocalData: UserStatsLocalData): SetSeenWalletOriginUseCase {
    return SetSeenWalletOriginUseCase(userStatsLocalData)
  }

  @Singleton
  @Provides
  fun providesSetSeenPromotionsUseCase(
      promotionsRepository: PromotionsRepository): SetSeenPromotionsUseCase {
    return SetSeenPromotionsUseCase(promotionsRepository)
  }

  @Singleton
  @Provides
  fun providesSetSelectedCurrencyUseCase(
      fiatCurrenciesRepository: FiatCurrenciesRepository): SetSelectedCurrencyUseCase {
    return SetSelectedCurrencyUseCase(fiatCurrenciesRepository)
  }

  @Singleton
  @Provides
  fun providesGetSelectedCurrencyUseCase(fiatCurrenciesRepository: FiatCurrenciesRepository,
                                         conversionService: LocalCurrencyConversionService): GetSelectedCurrencyUseCase {
    return GetSelectedCurrencyUseCase(fiatCurrenciesRepository, conversionService)
  }
}