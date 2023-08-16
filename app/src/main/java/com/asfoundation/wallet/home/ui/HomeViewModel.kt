package com.asfoundation.wallet.home.ui

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.properties.APTOIDE_TOP_APPS_URL
import com.appcoins.wallet.core.utils.properties.VIP_PROGRAM_BADGE_URL
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import com.appcoins.wallet.ui.arch.*
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource.TriggerSource
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource.TriggerSource.FIRST_PURCHASE
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource.TriggerSource.NEW_LEVEL
import com.appcoins.wallet.ui.arch.data.Async
import com.asfoundation.wallet.backup.triggers.TriggerUtils.toJson
import com.asfoundation.wallet.backup.use_cases.ShouldShowBackupTriggerUseCase
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.entity.GlobalBalance
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.gamification.ObserveUserStatsUseCase
import com.asfoundation.wallet.home.usecases.*
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.balance.TokenBalance
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase.Companion.PLAY_APP_VIEW_URL
import com.asfoundation.wallet.viewmodel.TransactionsWalletModel
import com.asfoundation.wallet.wallets.domain.WalletBalance
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

sealed class HomeSideEffect : SideEffect {
  data class NavigateToBrowser(val uri: Uri) : HomeSideEffect()
  data class NavigateToRateUs(val shouldNavigate: Boolean) : HomeSideEffect()
  data class NavigateToSettings(val turnOnFingerprint: Boolean = false) : HomeSideEffect()
  data class NavigateToShare(val url: String) : HomeSideEffect()
  data class NavigateToDetails(val transaction: Transaction, val balanceCurrency: String) :
    HomeSideEffect()

  data class NavigateToBackup(val walletAddress: String) : HomeSideEffect()
  data class NavigateToIntent(val intent: Intent) : HomeSideEffect()
  data class ShowBackupTrigger(
    val walletAddress: String,
    val triggerSource: TriggerSource
  ) : HomeSideEffect()

  object NavigateToMyWallets : HomeSideEffect()
  object NavigateToChangeCurrency : HomeSideEffect()
}

data class HomeState(
  val transactionsModelAsync: Async<TransactionsModel> = Async.Uninitialized,
  val defaultWalletBalanceAsync: Async<GlobalBalance> = Async.Uninitialized,
  val showVipBadge: Boolean = false,
  val unreadMessages: Boolean = false
) : ViewState

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val analytics: HomeAnalytics,
  private val backupTriggerPreferences: BackupTriggerPreferencesDataSource,
  private val shouldShowBackupTriggerUseCase: ShouldShowBackupTriggerUseCase,
  private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val shouldOpenRatingDialogUseCase: ShouldOpenRatingDialogUseCase,
  private val updateTransactionsNumberUseCase: UpdateTransactionsNumberUseCase,
  private val findNetworkInfoUseCase: FindNetworkInfoUseCase,
  private val fetchTransactionsUseCase: FetchTransactionsUseCase,
  private val findDefaultWalletUseCase: FindDefaultWalletUseCase,
  private val observeDefaultWalletUseCase: ObserveDefaultWalletUseCase,
  private val dismissCardNotificationUseCase: DismissCardNotificationUseCase,
  private val getLevelsUseCase: GetLevelsUseCase,
  private val getUserLevelUseCase: GetUserLevelUseCase,
  private val observeUserStatsUseCase: ObserveUserStatsUseCase,
  private val getLastShownUserLevelUseCase: GetLastShownUserLevelUseCase,
  private val updateLastShownUserLevelUseCase: UpdateLastShownUserLevelUseCase,
  private val getCardNotificationsUseCase: GetCardNotificationsUseCase,
  private val registerSupportUserUseCase: RegisterSupportUserUseCase,
  private val getUnreadConversationsCountEventsUseCase: GetUnreadConversationsCountEventsUseCase,
  private val displayChatUseCase: DisplayChatUseCase,
  private val displayConversationListOrChatUseCase: DisplayConversationListOrChatUseCase,
  @Named("package-name") private val walletPackageName: String,
  private val walletsEventSender: WalletsEventSender,
  private val rxSchedulers: RxSchedulers
) : BaseViewModel<HomeState, HomeSideEffect>(initialState()) {

  private val UPDATE_INTERVAL = 30 * DateUtils.SECOND_IN_MILLIS
  private val MINUS_ONE = BigDecimal("-1")
  private val refreshData = BehaviorSubject.createDefault(true)
  private val refreshCardNotifications = BehaviorSubject.createDefault(true)

  companion object {
    fun initialState(): HomeState {
      return HomeState()
    }
  }

  init {
    handleWalletData()
    verifyUserLevel()
    handleUnreadConversationCount()
    handleRateUsDialogVisibility()
    handleBackupTrigger()
  }

  private fun handleWalletData() {
    observeRefreshData()
      .switchMap { observeNetworkAndWallet() }
      .switchMap { observeWalletData(it) }
      .scopedSubscribe { e -> e.printStackTrace() }
  }

  private fun observeRefreshData(): Observable<Boolean> {
    return refreshData.filter { refreshData: Boolean? -> refreshData!! }
  }

  fun updateData() {
    refreshData.onNext(true)
  }

  fun stopRefreshingData() {
    refreshData.onNext(false)
  }

  private fun observeNetworkAndWallet(): Observable<TransactionsWalletModel> {
    return Observable.combineLatest(
      findNetworkInfoUseCase().toObservable(), observeDefaultWalletUseCase()
    ) { networkInfo,
        wallet ->
      val previousModel: TransactionsWalletModel? =
        state.transactionsModelAsync.value?.transactionsWalletModel
      val isNewWallet =
        previousModel == null || !previousModel.wallet.hasSameAddress(wallet.address)
      TransactionsWalletModel(networkInfo, wallet, isNewWallet)
    }
  }

  private fun observeWalletData(model: TransactionsWalletModel): Observable<Unit> {
    return Observable.mergeDelayError(
      observeBalance(),
      updateTransactions(model).subscribeOn(rxSchedulers.io),
      updateRegisterUser(model.wallet).toObservable()
    )
      .map {}
      .subscribeOn(rxSchedulers.io)
  }

  private fun updateRegisterUser(wallet: Wallet): Completable {
    return getUserLevelUseCase()
      .subscribeOn(rxSchedulers.io)
      .map { userLevel ->
        registerSupportUser(userLevel, wallet.address)
        true
      }
      .ignoreElement()
      .subscribeOn(rxSchedulers.io)
  }

  private fun registerSupportUser(level: Int, walletAddress: String) {
    registerSupportUserUseCase(level, walletAddress)
  }

  /**
   * Balance is refreshed every [UPDATE_INTERVAL] seconds, and stops while [refreshData] is false
   */
  private fun observeBalance(): Observable<GlobalBalance> {
    return Observable.interval(0, UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
      .flatMap { observeRefreshData() }
      .switchMap {
        observeWalletInfoUseCase(null, update = true)
          .map { walletInfo -> mapWalletValue(walletInfo.walletBalance) }
          .asAsyncToState(HomeState::defaultWalletBalanceAsync) {
            copy(defaultWalletBalanceAsync = it)
          }
      }
  }

  private fun mapWalletValue(walletBalance: WalletBalance): GlobalBalance {
    return GlobalBalance(
      walletBalance, shouldShow(walletBalance.appcBalance, 0.01),
      shouldShow(walletBalance.creditsBalance, 0.01),
      shouldShow(walletBalance.ethBalance, 0.0001)
    )
  }

  private fun shouldShow(tokenBalance: TokenBalance, threshold: Double): Boolean {
    return (tokenBalance.token.amount >= BigDecimal(threshold) &&
        tokenBalance.fiat.amount.toDouble() >= threshold)
  }

  private fun updateTransactions(
    walletModel: TransactionsWalletModel?
  ): Observable<TransactionsWalletModel> {
    if (walletModel == null) return Observable.empty()
    val retainValue = if (walletModel.isNewWallet) null else HomeState::transactionsModelAsync
    return Observable.combineLatest(
      getTransactions(walletModel.wallet), getCardNotifications(),
      getMaxBonus(), observeNetworkAndWallet()
    ) { transactions: List<Transaction>, notifications: List<CardNotification>, maxBonus: Double, transactionsWalletModel: TransactionsWalletModel ->
      createTransactionsModel(
        transactions, notifications, maxBonus, transactionsWalletModel
      )
    }
      .doOnNext { (transactions) -> updateTransactionsNumberUseCase(transactions) }
      .subscribeOn(rxSchedulers.io)
      .observeOn(rxSchedulers.main)
      .asAsyncToState(retainValue) { copy(transactionsModelAsync = it) }
      .map { walletModel }
  }

  private fun createTransactionsModel(
    transactions: List<Transaction>,
    notifications: List<CardNotification>, maxBonus: Double,
    transactionsWalletModel: TransactionsWalletModel
  ): TransactionsModel {
    return TransactionsModel(transactions, notifications, maxBonus, transactionsWalletModel)
  }

  /**
   * Transactions are refreshed every [.UPDATE_INTERVAL] seconds, and stops while [.refreshData] is
   * false
   */
  private fun getTransactions(wallet: Wallet): Observable<List<Transaction>>? {
    return Observable.interval(0, UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
      .flatMap { observeRefreshData() }
      .switchMap { fetchTransactionsUseCase(wallet.address) }
      .doOnNext {
        if (it.isNotEmpty() &&
          getTriggerSourceJson(wallet.address) ==
          TriggerSource.NOT_SEEN
        ) {
          backupTriggerPreferences.setTriggerState(
            walletAddress = wallet.address,
            active = true,
            triggerSource = FIRST_PURCHASE.toJson()
          )
        }
      }
      .subscribeOn(rxSchedulers.io)
      .onErrorReturnItem(emptyList())
  }

  private fun getCardNotifications(): Observable<List<CardNotification>> {
    return refreshCardNotifications.flatMapSingle { getCardNotificationsUseCase() }
      .subscribeOn(rxSchedulers.io)
      .onErrorReturnItem(emptyList())
  }

  private fun getMaxBonus(): Observable<Double> {
    return getLevelsUseCase()
      .subscribeOn(rxSchedulers.io)
      .flatMap { (status, list) ->
        if (status == Levels.Status.OK) {
          return@flatMap Single.just(list[list.size - 1].bonus)
        }
        Single.error(IllegalStateException(status.name))
      }
      .toObservable()
  }

  private fun verifyUserLevel() {
    findDefaultWalletUseCase()
      .flatMapObservable { wallet ->
        observeUserStatsUseCase()
          .flatMapSingle { gamificationStats ->
            val userLevel = gamificationStats.level
            val isVipLevel =
              gamificationStats.gamificationStatus == GamificationStatus.VIP ||
                  gamificationStats.gamificationStatus == GamificationStatus.VIP_MAX
            setState { copy(showVipBadge = isVipLevel) }
            getLastShownUserLevelUseCase(wallet.address).doOnSuccess { lastShownLevel ->
              if (userLevel > lastShownLevel) {
                updateLastShownUserLevelUseCase(wallet.address, userLevel)
                backupTriggerPreferences.setTriggerState(
                  walletAddress = wallet.address,
                  active = true,
                  triggerSource = NEW_LEVEL.toJson()
                )
              }
            }
          }
      }
      .scopedSubscribe { e -> e.printStackTrace() }
  }

  fun goToVipLink() {
    analytics.sendAction("vip_badge")
    val uri = Uri.parse(VIP_PROGRAM_BADGE_URL)
    sendSideEffect { HomeSideEffect.NavigateToBrowser(uri) }
  }

  private fun handleUnreadConversationCount() {
    observeRefreshData()
      .switchMap {
        getUnreadConversationsCountEventsUseCase().subscribeOn(rxSchedulers.main)
          .doOnNext { count: Int? ->
            setState { copy(unreadMessages = (count != null && count != 0)) }
          }
      }
      .scopedSubscribe { e -> e.printStackTrace() }
  }

  private fun handleRateUsDialogVisibility() {
    shouldOpenRatingDialogUseCase()
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { shouldShow ->
        sendSideEffect { HomeSideEffect.NavigateToRateUs(shouldShow) }
      }
      .scopedSubscribe { e -> e.printStackTrace() }
  }

  fun showSupportScreen(fromNotification: Boolean) {
    if (fromNotification) {
      displayConversationListOrChatUseCase
    } else {
      displayChatUseCase()
    }
  }

  fun onCurrencySelectorClick() {
    sendSideEffect { HomeSideEffect.NavigateToChangeCurrency }
  }

  fun onSettingsClick() {
    sendSideEffect { HomeSideEffect.NavigateToSettings() }
  }

  fun onBalanceClick() {
    sendSideEffect { HomeSideEffect.NavigateToMyWallets }
  }

  fun onTransactionDetailsClick(transaction: Transaction) {
    sendSideEffect {
      state.defaultWalletBalanceAsync.value?.let {
        HomeSideEffect.NavigateToDetails(transaction, it.walletBalance.creditsOnlyFiat.currency)
      }
    }
  }

  fun onNotificationClick(
    cardNotification: CardNotification,
    cardNotificationAction: CardNotificationAction
  ) {
    when (cardNotificationAction) {
      CardNotificationAction.DISMISS -> dismissNotification(cardNotification)
      CardNotificationAction.DISCOVER -> sendSideEffect {
        HomeSideEffect.NavigateToBrowser(Uri.parse(APTOIDE_TOP_APPS_URL))
      }
      CardNotificationAction.UPDATE -> {
        sendSideEffect { HomeSideEffect.NavigateToIntent(buildAutoUpdateIntent()) }
        dismissNotification(cardNotification)
      }
      CardNotificationAction.BACKUP -> {
        val model: TransactionsWalletModel? =
          state.transactionsModelAsync.value?.transactionsWalletModel
        if (model != null) {
          val wallet = model.wallet
          if (wallet.address != null) {
            sendSideEffect { HomeSideEffect.NavigateToBackup(wallet.address) }
            walletsEventSender.sendCreateBackupEvent(
              WalletsAnalytics.ACTION_CREATE,
              WalletsAnalytics.CONTEXT_CARD,
              WalletsAnalytics.STATUS_SUCCESS
            )
          }
        }
      }
      CardNotificationAction.NONE -> {}
    }
  }

  private fun handleBackupTrigger() {
    getWalletInfoUseCase(null, cached = false)
      .flatMap { walletInfo ->
        shouldShowBackupTriggerUseCase(walletInfo.wallet).map { shouldShow ->
          if (shouldShow &&
            backupTriggerPreferences.getTriggerState(walletInfo.wallet) &&
            !walletInfo.hasBackup
          ) {
            sendSideEffect {
              HomeSideEffect.ShowBackupTrigger(
                walletInfo.wallet,
                getTriggerSourceJson(walletInfo.wallet)
              )
            }
          }
        }
      }
      .scopedSubscribe()
  }

  private fun buildAutoUpdateIntent(): Intent {
    val intent =
      Intent(Intent.ACTION_VIEW, Uri.parse(String.format(PLAY_APP_VIEW_URL, walletPackageName)))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return intent
  }

  private fun dismissNotification(cardNotification: CardNotification) {
    dismissCardNotificationUseCase(cardNotification)
      .subscribeOn(rxSchedulers.main)
      .doOnComplete { refreshCardNotifications.onNext(true) }
      .scopedSubscribe { e -> e.printStackTrace() }
  }

  private fun getTriggerSourceJson(walletAddress: String) =
    Gson().fromJson(
      backupTriggerPreferences.getTriggerSource(walletAddress),
      TriggerSource::class.java
    )
}
