package com.asfoundation.wallet.home

import android.content.Context
import androidx.fragment.app.Fragment
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.home.usecases.*
import com.asfoundation.wallet.main.MainActivityNavigator
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ActivityContext

@InstallIn(FragmentComponent::class)
@Module
class HomeModule {

  @Provides
  fun providesHomeNavigator(fragment: Fragment): HomeNavigator {
    return HomeNavigator(fragment, MainActivityNavigator(fragment.requireActivity()))
  }

//  @Provides
//  fun provideMainActivityNavigator(fragment: Fragment): MainActivityNavigator {
//    return MainActivityNavigator(fragment.requireActivity())
//  }

  @Provides
  fun providesHomeViewModelFactory(homeAnalytics: HomeAnalytics,
                                   observeWalletInfoUseCase: ObserveWalletInfoUseCase,
                                   shouldOpenRatingDialogUseCase: ShouldOpenRatingDialogUseCase,
                                   updateTransactionsNumberUseCase: UpdateTransactionsNumberUseCase,
                                   findNetworkInfoUseCase: FindNetworkInfoUseCase,
                                   fetchTransactionsUseCase: FetchTransactionsUseCase,
                                   findDefaultWalletUseCase: FindDefaultWalletUseCase,
                                   observeDefaultWalletUseCase: ObserveDefaultWalletUseCase,
                                   dismissCardNotificationUseCase: DismissCardNotificationUseCase,
                                   shouldShowFingerprintTooltipUseCase: ShouldShowFingerprintTooltipUseCase,
                                   setSeenFingerprintTooltipUseCase: SetSeenFingerprintTooltipUseCase,
                                   getLevelsUseCase: GetLevelsUseCase,
                                   getUserLevelUseCase: GetUserLevelUseCase,
                                   getCardNotificationsUseCase: GetCardNotificationsUseCase,
                                   registerSupportUserUseCase: RegisterSupportUserUseCase,
                                   getUnreadConversationsCountEventsUseCase: GetUnreadConversationsCountEventsUseCase,
                                   displayChatUseCase: DisplayChatUseCase,
                                   displayConversationListOrChatUseCase: DisplayConversationListOrChatUseCase,
                                   @ActivityContext context: Context,
                                   walletsEventSender: WalletsEventSender): HomeViewModelFactory {
    return HomeViewModelFactory(homeAnalytics, observeWalletInfoUseCase,
        shouldOpenRatingDialogUseCase, updateTransactionsNumberUseCase, findNetworkInfoUseCase,
        fetchTransactionsUseCase, findDefaultWalletUseCase, observeDefaultWalletUseCase,
        dismissCardNotificationUseCase, shouldShowFingerprintTooltipUseCase,
        setSeenFingerprintTooltipUseCase, getLevelsUseCase, getUserLevelUseCase,
        getCardNotificationsUseCase, registerSupportUserUseCase,
        getUnreadConversationsCountEventsUseCase, displayChatUseCase,
        displayConversationListOrChatUseCase, context.packageName, walletsEventSender)
  }
}