package com.asfoundation.wallet.promotions.ui

import com.asfoundation.wallet.analytics.RakamAnalytics
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
                                        rakamAnalytics: RakamAnalytics,
                                        setSeenPromotionsUseCase: SetSeenPromotionsUseCase,
                                        setSeenWalletOriginUseCase: SetSeenWalletOriginUseCase): PromotionsViewModelFactory {
    return PromotionsViewModelFactory(getPromotionsUseCase, rakamAnalytics,
        setSeenPromotionsUseCase, setSeenWalletOriginUseCase, Schedulers.io())
  }
}