package com.asfoundation.wallet.promotions.ui

import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.promotions.usecases.GetPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenWalletOriginUseCase
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers

@Module
class PromotionsModuleNew {

  @Provides
  fun providesPromotionsNavigator(fragment: PromotionsFragment): PromotionsNavigator {
    return PromotionsNavigator(fragment)
  }

  @Provides
  fun providesPromotionViewModelFactory(getPromotionsUseCase: GetPromotionsUseCase,
                                        analyticsSetup: AnalyticsSetup,
                                        setSeenPromotionsUseCase: SetSeenPromotionsUseCase,
                                        setSeenWalletOriginUseCase: SetSeenWalletOriginUseCase): PromotionsViewModelFactory {
    return PromotionsViewModelFactory(getPromotionsUseCase, analyticsSetup,
        setSeenPromotionsUseCase, setSeenWalletOriginUseCase, Schedulers.io())
  }
}