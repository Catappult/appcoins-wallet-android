package com.asfoundation.wallet.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.home.usecases.*
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class HomeViewModelFactory(private val analytics: HomeAnalytics,
                           private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
                           private val shouldOpenRatingDialogUseCase: ShouldOpenRatingDialogUseCase,
                           private val updateTransactionsNumberUseCase: UpdateTransactionsNumberUseCase,
                           private val findNetworkInfoUseCase: FindNetworkInfoUseCase,
                           private val fetchTransactionsUseCase: FetchTransactionsUseCase,
                           private val findDefaultWalletUseCase: FindDefaultWalletUseCase,
                           private val observeDefaultWalletUseCase: ObserveDefaultWalletUseCase,
                           private val dismissCardNotificationUseCase: DismissCardNotificationUseCase,
                           private val shouldShowFingerprintTooltipUseCase: ShouldShowFingerprintTooltipUseCase,
                           private val setSeenFingerprintTooltipUseCase: SetSeenFingerprintTooltipUseCase,
                           private val getLevelsUseCase: GetLevelsUseCase,
                           private val getUserLevelUseCase: GetUserLevelUseCase,
                           private val getCardNotificationsUseCase: GetCardNotificationsUseCase,
                           private val registerSupportUserUseCase: RegisterSupportUserUseCase,
                           private val getUnreadConversationsCountEventsUseCase: GetUnreadConversationsCountEventsUseCase,
                           private val displayChatUseCase: DisplayChatUseCase,
                           private val displayConversationListOrChatUseCase: DisplayConversationListOrChatUseCase,
                           private val walletPackageName: String,
                           private val walletsEventSender: WalletsEventSender) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return HomeViewModel(analytics, observeWalletInfoUseCase,
        shouldOpenRatingDialogUseCase, updateTransactionsNumberUseCase, findNetworkInfoUseCase,
        fetchTransactionsUseCase, findDefaultWalletUseCase,
        observeDefaultWalletUseCase, dismissCardNotificationUseCase,
        shouldShowFingerprintTooltipUseCase, setSeenFingerprintTooltipUseCase, getLevelsUseCase,
        getUserLevelUseCase, getCardNotificationsUseCase, registerSupportUserUseCase,
        getUnreadConversationsCountEventsUseCase, displayChatUseCase,
        displayConversationListOrChatUseCase, walletPackageName, walletsEventSender,
        AndroidSchedulers.mainThread(), Schedulers.io()) as T
  }
}