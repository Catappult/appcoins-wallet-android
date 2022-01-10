package com.asfoundation.wallet.home

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.util.Log
import com.appcoins.wallet.gamification.repository.Levels
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.base.*
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.entity.GlobalBalance
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.home.usecases.*
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.balance.TokenBalance
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import com.asfoundation.wallet.viewmodel.TransactionsWalletModel
import com.asfoundation.wallet.wallets.domain.WalletBalance
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

sealed class HomeSideEffect : SideEffect {
  data class NavigateToBrowser(val uri: Uri) : HomeSideEffect()
  data class NavigateToRateUs(val shouldNavigate: Boolean) : HomeSideEffect()
  data class NavigateToReceive(val wallet: Wallet?) : HomeSideEffect()
  data class NavigateToSettings(val turnOnFingerprint: Boolean = false) : HomeSideEffect()
  data class NavigateToShare(val url: String) : HomeSideEffect()
  data class NavigateToDetails(val transaction: Transaction, val balanceCurrency: String) :
      HomeSideEffect()

  data class NavigateToBackup(val walletAddress: String) : HomeSideEffect()
  data class NavigateToIntent(val intent: Intent) : HomeSideEffect()
  object NavigateToMyWallets : HomeSideEffect()
  object NavigateToSend : HomeSideEffect()
  object NavigateToChangeCurrency : HomeSideEffect()
  object ShowFingerprintTooltip : HomeSideEffect()
}

data class HomeState(val transactionsModelAsync: Async<TransactionsModel> = Async.Uninitialized,
                     val defaultWalletBalanceAsync: Async<GlobalBalance> = Async.Uninitialized,
                     val showVipBadge: Boolean = false,
                     val unreadMessages: Boolean = false) : ViewState

@HiltViewModel
class HomeViewModel @Inject constructor(private val analytics: HomeAnalytics,
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
                    @Named("package-name") private val walletPackageName: String,
                    private val walletsEventSender: WalletsEventSender,
                    private val rxSchedulers: RxSchedulers) :
    BaseViewModel<HomeState, HomeSideEffect>(initialState()) {

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
    handleFingerprintTooltipVisibility()
  }

  private fun handleWalletData() {
    observeRefreshData().switchMap { observeNetworkAndWallet() }
        .switchMap { observeWalletData(it) }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
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
    return Observable.combineLatest(findNetworkInfoUseCase()
        .toObservable(), observeDefaultWalletUseCase(),
        { networkInfo, wallet ->
          val previousModel: TransactionsWalletModel? =
              state.transactionsModelAsync.value?.transactionsWalletModel
          val isNewWallet = previousModel == null || !previousModel.wallet
              .sameAddress(wallet.address)
          TransactionsWalletModel(networkInfo, wallet, isNewWallet)
        })
  }

  private fun observeWalletData(model: TransactionsWalletModel): Observable<Unit> {
    return Observable.mergeDelayError(
        observeBalance(),
        updateTransactions(model).subscribeOn(rxSchedulers.io),
        updateRegisterUser(model.wallet).toObservable()
    )
        .map { }
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
   * Balance is refreshed every [UPDATE_INTERVAL] seconds, and stops while
   * [refreshData] is false
   */
  private fun observeBalance(): Observable<GlobalBalance> {
    return Observable.interval(0, UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
        .flatMap { observeRefreshData() }
        .switchMap {
          observeWalletInfoUseCase(null, update = true, updateFiat = true)
              .map { walletInfo -> mapWalletValue(walletInfo.walletBalance) }
              .asAsyncToState(HomeState::defaultWalletBalanceAsync) {
                copy(defaultWalletBalanceAsync = it)
              }
        }
  }

  private fun mapWalletValue(walletBalance: WalletBalance): GlobalBalance {
    return GlobalBalance(walletBalance, shouldShow(walletBalance.appcBalance, 0.01),
        shouldShow(walletBalance.creditsBalance, 0.01),
        shouldShow(walletBalance.ethBalance, 0.0001))
  }

  private fun shouldShow(tokenBalance: TokenBalance, threshold: Double): Boolean {
    return (tokenBalance.token.amount >= BigDecimal(
        threshold) && tokenBalance.fiat.amount.toDouble() >= threshold)
  }

  private fun updateTransactions(
      walletModel: TransactionsWalletModel?): Observable<TransactionsWalletModel> {
    if (walletModel == null) return Observable.empty()
    val retainValue = if (walletModel.isNewWallet) null else HomeState::transactionsModelAsync
    return Observable.combineLatest(getTransactions(walletModel.wallet), getCardNotifications(),
        getMaxBonus(), observeNetworkAndWallet(),
        { transactions: List<Transaction>, notifications: List<CardNotification>, maxBonus: Double, transactionsWalletModel: TransactionsWalletModel ->
          createTransactionsModel(transactions, notifications, maxBonus,
              transactionsWalletModel)
        })
        .doOnNext { (transactions) ->
          updateTransactionsNumberUseCase(
              transactions)
        }
        .subscribeOn(rxSchedulers.io)
        .observeOn(rxSchedulers.main)
        .asAsyncToState(retainValue) { copy(transactionsModelAsync = it) }
        .map { walletModel }
  }

  private fun createTransactionsModel(transactions: List<Transaction>,
                                      notifications: List<CardNotification>, maxBonus: Double,
                                      transactionsWalletModel: TransactionsWalletModel): TransactionsModel {
    return TransactionsModel(transactions, notifications, maxBonus, transactionsWalletModel)
  }

  /**
   * Transactions are refreshed every [.UPDATE_INTERVAL] seconds, and stops while
   * [.refreshData] is false
   */
  private fun getTransactions(wallet: Wallet): Observable<List<Transaction>>? {
    return Observable.interval(0, UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
        .flatMap { observeRefreshData() }
        .switchMap {
          fetchTransactionsUseCase(wallet.address)
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
          Log.d("Hilt-", "getMaxBonus: status -> $status -- list -> $list")
          if (status
              == Levels.Status.OK) {
            return@flatMap Single.just(list[list
                .size - 1]
                .bonus)
          }
          Single.error(IllegalStateException(status
              .name))
        }
        .toObservable()
  }

  private fun verifyUserLevel() {
    findDefaultWalletUseCase()
        .subscribeOn(rxSchedulers.io)
        .flatMap {
          getUserLevelUseCase()
              .subscribeOn(rxSchedulers.io)
              .doOnSuccess { userLevel: Int ->
                setState { copy(showVipBadge = (userLevel == 9 || userLevel == 10)) }
              }
        }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
  }

  fun goToVipLink() {
    analytics.sendAction("vip_badge")
    val uri = Uri.parse(BuildConfig.VIP_PROGRAM_BADGE_URL)
    sendSideEffect { HomeSideEffect.NavigateToBrowser(uri) }
  }

  private fun handleUnreadConversationCount() {
    observeRefreshData().switchMap {
      getUnreadConversationsCountEventsUseCase()
          .subscribeOn(rxSchedulers.main)
          .doOnNext { count: Int? ->
            setState { copy(unreadMessages = (count != null && count != 0)) }
          }
    }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
  }

  private fun handleRateUsDialogVisibility() {
    shouldOpenRatingDialogUseCase()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess { shouldShow ->
          sendSideEffect { HomeSideEffect.NavigateToRateUs(shouldShow) }
        }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
  }

  private fun handleFingerprintTooltipVisibility() {
    shouldShowFingerprintTooltipUseCase(BuildConfig.APPLICATION_ID)
        .doOnSuccess { value ->
          if (value == true) {
            sendSideEffect { HomeSideEffect.ShowFingerprintTooltip }
          }
        }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
  }

  fun onTurnFingerprintOnClick() {
    sendSideEffect { HomeSideEffect.NavigateToSettings(turnOnFingerprint = true) }
    setSeenFingerprintTooltipUseCase()
  }

  fun onFingerprintDismissed() {
    setSeenFingerprintTooltipUseCase()
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

  fun onSendClick() {
    sendSideEffect { HomeSideEffect.NavigateToSend }
  }

  fun onReceiveClick() {
    sendSideEffect {
      state.transactionsModelAsync.value?.transactionsWalletModel?.let {
        HomeSideEffect.NavigateToReceive(
            it.wallet)
      }
    }
  }

  fun onTransactionDetailsClick(transaction: Transaction) {
    sendSideEffect {
      state.defaultWalletBalanceAsync.value?.let {
        HomeSideEffect.NavigateToDetails(transaction, it.walletBalance.overallFiat.currency)
      }
    }
  }

  fun onNotificationClick(cardNotification: CardNotification,
                          cardNotificationAction: CardNotificationAction) {
    when (cardNotificationAction) {
      CardNotificationAction.DISMISS -> dismissNotification(cardNotification)
      CardNotificationAction.DISCOVER -> sendSideEffect {
        HomeSideEffect.NavigateToBrowser(Uri.parse(BuildConfig.APTOIDE_TOP_APPS_URL))
      }
      CardNotificationAction.UPDATE -> {
        sendSideEffect {
//          HomeSideEffect.NavigateToIntent(buildAutoUpdateIntentUseCase())
          HomeSideEffect.NavigateToIntent(buildAutoUpdateIntent())
        }
        dismissNotification(cardNotification)
      }
      CardNotificationAction.BACKUP -> {
        val model: TransactionsWalletModel? =
            state.transactionsModelAsync.value?.transactionsWalletModel
        if (model != null) {
          val wallet = model.wallet
          if (wallet.address != null) {
            sendSideEffect { HomeSideEffect.NavigateToBackup(wallet.address) }
            walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
                WalletsAnalytics.CONTEXT_CARD, WalletsAnalytics.STATUS_SUCCESS)
          }
        }
      }
      CardNotificationAction.NONE -> {
      }
    }
  }

  private fun buildAutoUpdateIntent(): Intent {
    val intent =
        Intent(Intent.ACTION_VIEW,
            Uri.parse(String.format(AutoUpdateInteract.PLAY_APP_VIEW_URL, walletPackageName)))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return intent
  }

  private fun dismissNotification(cardNotification: CardNotification) {
    dismissCardNotificationUseCase(cardNotification)
        .subscribeOn(rxSchedulers.main)
        .doOnComplete { refreshCardNotifications.onNext(true) }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
  }
}