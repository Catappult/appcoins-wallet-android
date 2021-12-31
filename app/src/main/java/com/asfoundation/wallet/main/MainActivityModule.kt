package com.asfoundation.wallet.main

import android.app.Activity
import com.asfoundation.wallet.home.usecases.DisplayConversationListOrChatUseCase
import com.asfoundation.wallet.main.usecases.HasSeenPromotionTooltipUseCase
import com.asfoundation.wallet.main.usecases.IncreaseLaunchCountUseCase
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.support.SupportNotificationProperties
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@InstallIn(ActivityComponent::class)
@Module
class MainActivityModule {

  @Provides
  fun provideNavigator(activity: Activity): MainActivityNavigator {
    return MainActivityNavigator(activity)
  }

  @Provides
  fun provideData(activity: Activity): MainData {
    return MainData(
        activity.intent.getBooleanExtra(SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK,
            false)
    )
  }

  @Provides
  fun provideMainViewModelFactory(data: MainData,
                                  hasSeenPromotionTooltipUseCase: HasSeenPromotionTooltipUseCase,
                                  increaseLaunchCountUseCase: IncreaseLaunchCountUseCase,
                                  promotionsInteractor: PromotionsInteractor,
                                  displayConversationListOrChatUseCase: DisplayConversationListOrChatUseCase): MainViewModelFactory {
    return MainViewModelFactory(data, hasSeenPromotionTooltipUseCase, increaseLaunchCountUseCase,
        promotionsInteractor, displayConversationListOrChatUseCase)
  }
}