package com.asfoundation.wallet.promotions.ui

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.analytics.RakamAnalytics
import com.asfoundation.wallet.promotions.usecases.GetPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenWalletOriginUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.schedulers.Schedulers
@InstallIn(FragmentComponent::class)
@Module
class PromotionsModule {

  @Provides
  fun providesPromotionsNavigator(fragment: Fragment): PromotionsNavigator {
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