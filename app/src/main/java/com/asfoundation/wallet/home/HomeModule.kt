package com.asfoundation.wallet.home

import android.content.Context
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.home.usecases.*
import com.asfoundation.wallet.main.MainActivityNavigator
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
                                   shouldShowFingerprintTooltipUseCase: ShouldShowFingerprintTooltipUseCase,
                                   setSeenFingerprintTooltipUseCase: SetSeenFingerprintTooltipUseCase,
                                   getLevelsUseCase: GetLevelsUseCase,
                                   getUserLevelUseCase: GetUserLevelUseCase,
                                   getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase,
                                   getAppcBalanceUseCase: GetAppcBalanceUseCase,
                                   getEthBalanceUseCase: GetEthBalanceUseCase,
                                   getCreditsBalanceUseCase: GetCreditsBalanceUseCase,
                                   getCardNotificationsUseCase: GetCardNotificationsUseCase,
                                   registerSupportUserUseCase: RegisterSupportUserUseCase,
                                   getUnreadConversationsCountEventsUseCase: GetUnreadConversationsCountEventsUseCase,
                                   displayChatUseCase: DisplayChatUseCase,
                                   displayConversationListOrChatUseCase: DisplayConversationListOrChatUseCase,
                                   context: Context,
                                   walletsEventSender: WalletsEventSender,
                                   currencyFormatUtils: CurrencyFormatUtils): HomeViewModelFactory {
    return HomeViewModelFactory(homeAnalytics, shouldOpenRatingDialogUseCase,
        updateTransactionsNumberUseCase, findNetworkInfoUseCase, fetchTransactionsUseCase,
        findDefaultWalletUseCase, observeDefaultWalletUseCase, dismissCardNotificationUseCase,
        shouldShowFingerprintTooltipUseCase,
        setSeenFingerprintTooltipUseCase, getLevelsUseCase, getUserLevelUseCase,
        getSelectedCurrencyUseCase,
        getAppcBalanceUseCase, getEthBalanceUseCase, getCreditsBalanceUseCase,
        getCardNotificationsUseCase, registerSupportUserUseCase,
        getUnreadConversationsCountEventsUseCase, displayChatUseCase,
        displayConversationListOrChatUseCase, context.packageName, walletsEventSender,
        currencyFormatUtils)
  }
}