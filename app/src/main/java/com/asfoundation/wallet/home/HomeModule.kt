package com.asfoundation.wallet.home

import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.home.usecases.*
import com.asfoundation.wallet.main.MainActivityNavigator
import com.asfoundation.wallet.ui.AppcoinsApps
import com.asfoundation.wallet.util.CurrencyFormatUtils
import dagger.Module
import dagger.Provides

@Module
class HomeModule {

  @Provides
  fun providesHomeNavigator(fragment: HomeFragment): HomeNavigator {
    return HomeNavigator(fragment, MainActivityNavigator(fragment.requireActivity()))
  }

  @Provides
  fun provideMainActivityNavigator(fragment: HomeFragment): MainActivityNavigator {
    return MainActivityNavigator(fragment.requireActivity())
  }

  @Provides
  fun providesHomeViewModelFactory(homeAnalytics: HomeAnalytics,
                                   shouldOpenRatingDialogUseCase: ShouldOpenRatingDialogUseCase,
                                   updateTransactionsNumberUseCase: UpdateTransactionsNumberUseCase,
                                   findNetworkInfoUseCase: FindNetworkInfoUseCase,
                                   fetchTransactionsUseCase: FetchTransactionsUseCase,
                                   findDefaultWalletUseCase: FindDefaultWalletUseCase,
                                   observeDefaultWalletUseCase: ObserveDefaultWalletUseCase,
                                   dismissCardNotificationUseCase: DismissCardNotificationUseCase,
                                   buildAutoUpdateIntentUseCase: BuildAutoUpdateIntentUseCase,
                                   shouldShowFingerprintTooltipUseCase: ShouldShowFingerprintTooltipUseCase,
                                   setSeenFingerprintTooltipUseCase: SetSeenFingerprintTooltipUseCase,
                                   getLevelsUseCase: GetLevelsUseCase,
                                   getUserLevelUseCase: GetUserLevelUseCase,
                                   getAppcBalanceUseCase: GetAppcBalanceUseCase,
                                   getEthBalanceUseCase: GetEthBalanceUseCase,
                                   getCreditsBalanceUseCase: GetCreditsBalanceUseCase,
                                   getCardNotificationsUseCase: GetCardNotificationsUseCase,
                                   registerSupportUserUseCase: RegisterSupportUserUseCase,
                                   getUnreadConversationsCountEventsUseCase: GetUnreadConversationsCountEventsUseCase,
                                   displayChatUseCase: DisplayChatUseCase,
                                   displayConversationListOrChatUseCase: DisplayConversationListOrChatUseCase,
                                   walletsEventSender: WalletsEventSender,
                                   currencyFormatUtils: CurrencyFormatUtils): HomeViewModelFactory {
    return HomeViewModelFactory(homeAnalytics, shouldOpenRatingDialogUseCase,
        updateTransactionsNumberUseCase, findNetworkInfoUseCase, fetchTransactionsUseCase,
        findDefaultWalletUseCase, observeDefaultWalletUseCase, dismissCardNotificationUseCase,
        buildAutoUpdateIntentUseCase, shouldShowFingerprintTooltipUseCase,
        setSeenFingerprintTooltipUseCase, getLevelsUseCase, getUserLevelUseCase,
        getAppcBalanceUseCase, getEthBalanceUseCase, getCreditsBalanceUseCase,
        getCardNotificationsUseCase, registerSupportUserUseCase,
        getUnreadConversationsCountEventsUseCase, displayChatUseCase,
        displayConversationListOrChatUseCase, walletsEventSender, currencyFormatUtils)
  }
}