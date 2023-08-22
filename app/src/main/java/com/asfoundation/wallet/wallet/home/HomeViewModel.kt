package com.asfoundation.wallet.wallet.home

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.analytics.analytics.legacy.HomeAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import com.appcoins.wallet.core.network.base.call_adapter.ApiException
import com.appcoins.wallet.core.network.base.call_adapter.ApiFailure
import com.appcoins.wallet.core.network.base.call_adapter.ApiSuccess
import com.appcoins.wallet.core.utils.android_common.DateFormatterUtils.getDay
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.utils.properties.APTOIDE_TOP_APPS_URL
import com.appcoins.wallet.core.utils.properties.VIP_PROGRAM_BADGE_URL
import com.appcoins.wallet.feature.backup.data.use_cases.ShouldShowBackupTriggerUseCase
import com.appcoins.wallet.feature.backup.ui.triggers.TriggerUtils.toJson
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetSelectedCurrencyUseCase
import com.appcoins.wallet.feature.walletInfo.data.balance.TokenBalance
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletBalance
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.ObserveWalletInfoUseCase
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource.TriggerSource
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource.TriggerSource.NEW_LEVEL
import com.appcoins.wallet.ui.widgets.CardPromotionItem
import com.appcoins.wallet.ui.widgets.GameData
import com.asfoundation.wallet.entity.GlobalBalance
import com.asfoundation.wallet.gamification.ObserveUserStatsUseCase
import com.asfoundation.wallet.home.usecases.*
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.promotions.ui.PromotionsState
import com.asfoundation.wallet.promotions.usecases.GetPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenPromotionsUseCase
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.transactions.TransactionModel
import com.asfoundation.wallet.transactions.toModel
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase.Companion.PLAY_APP_VIEW_URL
import com.asfoundation.wallet.viewmodel.TransactionsWalletModel
import com.asfoundation.wallet.wallet.home.HomeViewModel.UiState.Success
import com.github.michaelbull.result.unwrap
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.rxSingle
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

sealed class HomeSideEffect : SideEffect {
  data class NavigateToBrowser(val uri: Uri) : HomeSideEffect()
  data class NavigateToRateUs(val shouldNavigate: Boolean) : HomeSideEffect()
  data class NavigateToSettings(val turnOnFingerprint: Boolean = false) : HomeSideEffect()
  data class NavigateToBackup(val walletAddress: String, val walletName: String) : HomeSideEffect()
  data class NavigateToIntent(val intent: Intent) : HomeSideEffect()
  data class ShowBackupTrigger(val walletAddress: String, val triggerSource: TriggerSource) :
    HomeSideEffect()

  object NavigateToChangeCurrency : HomeSideEffect()
  object NavigateToTopUp : HomeSideEffect()
  object NavigateToTransfer : HomeSideEffect()
  object NavigateToTransactionsList : HomeSideEffect()
  object NavigateToRecover : HomeSideEffect()
}

data class HomeState(
  val transactionsModelAsync: Async<TransactionsModel> = Async.Uninitialized,
  val promotionsModelAsync: Async<PromotionsModel> = Async.Uninitialized,
  val defaultWalletBalanceAsync: Async<GlobalBalance> = Async.Uninitialized,
  val showVipBadge: Boolean = false,
  val unreadMessages: Boolean = false,
  val hasBackup: Async<Boolean> = Async.Uninitialized
) : ViewState

@HiltViewModel
class HomeViewModel
@Inject
constructor(
  private val analytics: HomeAnalytics,
  private val backupTriggerPreferences: BackupTriggerPreferencesDataSource,
  private val shouldShowBackupTriggerUseCase: ShouldShowBackupTriggerUseCase,
  private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val getPromotionsUseCase: GetPromotionsUseCase,
  private val setSeenPromotionsUseCase: SetSeenPromotionsUseCase,
  private val shouldOpenRatingDialogUseCase: ShouldOpenRatingDialogUseCase,
  private val findNetworkInfoUseCase: FindNetworkInfoUseCase,
  private val findDefaultWalletUseCase: FindDefaultWalletUseCase,
  private val observeDefaultWalletUseCase: ObserveDefaultWalletUseCase,
  private val dismissCardNotificationUseCase: DismissCardNotificationUseCase,
  private val getGamesListingUseCase: GetGamesListingUseCase,
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
  private val fetchTransactionsHistoryUseCase: FetchTransactionsHistoryUseCase,
  private val getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase,
  @Named("package-name") private val walletPackageName: String,
  private val walletsEventSender: WalletsEventSender,
  private val rxSchedulers: RxSchedulers,
  private val dispatchers: Dispatchers,
  private val logger: Logger
) : BaseViewModel<HomeState, HomeSideEffect>(initialState()) {

  private lateinit var defaultCurrency: String
  private val UPDATE_INTERVAL = 30 * DateUtils.SECOND_IN_MILLIS
  private val refreshData = BehaviorSubject.createDefault(true)
  private val refreshCardNotifications = BehaviorSubject.createDefault(true)
  val balance = mutableStateOf(FiatValue())
  val showBackup = mutableStateOf(false)
  val newWallet = mutableStateOf(false)
  val gamesList = mutableStateOf(listOf<GameData>())
  val activePromotions = mutableStateListOf<CardPromotionItem>()
  var walletName : String = ""

  companion object {
    private val TAG = HomeViewModel::class.java.name
    fun initialState() = HomeState()
  }

  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  init {
    handleWalletData()
    verifyUserLevel()
    handleUnreadConversationCount()
    handleRateUsDialogVisibility()
    handleBackupTrigger()
    fetchPromotions()
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
      updateRegisterUser(model.wallet).toObservable(),
      observeBackup()
    )
      .map {}
      .subscribeOn(rxSchedulers.io)
  }

  private fun fetchTransactionData() {
    Observable.combineLatest(
      rxSingle { getSelectedCurrencyUseCase(false) }.toObservable(), observeDefaultWalletUseCase()
    ) { selectedCurrency, wallet ->
      defaultCurrency = selectedCurrency.unwrap()
      fetchTransactions(wallet, defaultCurrency)
    }.subscribe()
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
        observeWalletInfoUseCase(null, update = true, updateFiat = true)
          .map { walletInfo -> mapWalletValue(walletInfo.walletBalance) }
          .asAsyncToState(HomeState::defaultWalletBalanceAsync) {
            copy(defaultWalletBalanceAsync = it)
          }
      }
      .doOnNext { fetchTransactionData() }
  }

  /**
   * Balance is refreshed every [UPDATE_INTERVAL] seconds, and stops while [refreshData] is false
   */
  private fun observeBackup(): Observable<Boolean> {
    return Observable.interval(0, UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
      .flatMap { observeRefreshData() }
      .switchMap {
        observeWalletInfoUseCase(null, update = true, updateFiat = true)
          .map { walletInfo -> walletInfo.hasBackup }
          .asAsyncToState(HomeState::hasBackup) {
            copy(hasBackup = it)
          }
      }
  }

  private fun mapWalletValue(walletBalance: WalletBalance): GlobalBalance {
    return GlobalBalance(
      walletBalance,
      shouldShow(walletBalance.appcBalance, 0.01),
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
      getCardNotifications(),
      getMaxBonus(), observeNetworkAndWallet()
    ) { notifications: List<CardNotification>, maxBonus: Double, transactionsWalletModel: TransactionsWalletModel ->
      createTransactionsModel(
        notifications, maxBonus, transactionsWalletModel
      )
    }
      .subscribeOn(rxSchedulers.io)
      .observeOn(rxSchedulers.main)
      .asAsyncToState(retainValue) { copy(transactionsModelAsync = it) }
      .map { walletModel }
  }

  private fun createTransactionsModel(
    notifications: List<CardNotification>, maxBonus: Double,
    transactionsWalletModel: TransactionsWalletModel
  ): TransactionsModel {
    return TransactionsModel(notifications, maxBonus, transactionsWalletModel)
  }

  private fun fetchTransactions(wallet: Wallet, selectedCurrency: String) {
    viewModelScope.launch {
      fetchTransactionsHistoryUseCase(
        wallet = wallet.address, limit = 4, currency = selectedCurrency
      )
        .catch { logger.log(TAG, it) }
        .collect { result ->
          when (result) {
            is ApiSuccess -> {
              newWallet.value = result.data.isEmpty()
              _uiState.value = Success(
                result.data
                  .map { it.toModel(defaultCurrency) }
                  .take(
                    with(result.data) {
                      if (size < 4 || last().txId == get(lastIndex - 1).parentTxId) size
                      else size - 1
                    }
                  )
                  .groupBy { it.date.getDay() }
              )
            }

            is ApiFailure -> {}
            is ApiException -> {}
          }
        }
    }
  }

  private fun getCardNotifications(): Observable<List<CardNotification>> {
    return refreshCardNotifications
      .flatMapSingle { getCardNotificationsUseCase() }
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

  fun fetchGamesListing() {
    getGamesListingUseCase()
      .subscribeOn(rxSchedulers.io)
      .scopedSubscribe({ gamesList.value = it }, { e -> e.printStackTrace() })
  }

  private fun verifyUserLevel() {
    findDefaultWalletUseCase()
      .flatMapObservable { wallet ->
        observeUserStatsUseCase().flatMapSingle { gamificationStats ->
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

  fun onTopUpClick() {
    sendSideEffect { HomeSideEffect.NavigateToTopUp }
  }

  fun onTransferClick() {
    sendSideEffect { HomeSideEffect.NavigateToTransfer }
  }


  fun onBackupClick() {
    val model: TransactionsWalletModel? =
      state.transactionsModelAsync.value?.transactionsWalletModel
    if (model != null) {
      val wallet = model.wallet
      sendSideEffect { HomeSideEffect.NavigateToBackup(wallet.address, walletName) }
      walletsEventSender.sendCreateBackupEvent(
        WalletsAnalytics.ACTION_CREATE,
        WalletsAnalytics.CONTEXT_CARD,
        WalletsAnalytics.STATUS_SUCCESS
      )
    }
  }

  fun onRecoverClick() = sendSideEffect { HomeSideEffect.NavigateToRecover }

  fun onNotificationClick(
    cardNotification: CardNotification,
    cardNotificationAction: CardNotificationAction
  ) {
    when (cardNotificationAction) {
      CardNotificationAction.DISMISS -> dismissNotification(cardNotification)
      CardNotificationAction.DISCOVER ->
        sendSideEffect { HomeSideEffect.NavigateToBrowser(Uri.parse(APTOIDE_TOP_APPS_URL)) }

      CardNotificationAction.UPDATE -> {
        sendSideEffect { HomeSideEffect.NavigateToIntent(buildAutoUpdateIntent()) }
        dismissNotification(cardNotification)
      }

      CardNotificationAction.BACKUP -> {
        onBackupClick()
      }

      CardNotificationAction.NONE -> {}
    }
  }

  fun onSeeAllTransactionsClick() = sendSideEffect { HomeSideEffect.NavigateToTransactionsList }

  private fun handleBackupTrigger() {
    getWalletInfoUseCase(null, cached = false, updateFiat = false)
      .flatMap { walletInfo ->
        walletName = walletInfo.name
        rxSingle(dispatchers.io) { shouldShowBackupTriggerUseCase(walletInfo.wallet)}.map { shouldShow ->
          if (shouldShow &&
            backupTriggerPreferences.getTriggerState(walletInfo.wallet) &&
            !walletInfo.hasBackup
          ) {
            sendSideEffect {
              HomeSideEffect.ShowBackupTrigger(
                walletInfo.wallet, getTriggerSourceJson(walletInfo.wallet)
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
    Gson()
      .fromJson(
        backupTriggerPreferences.getTriggerSource(walletAddress), TriggerSource::class.java
      )

  fun fetchPromotions() {
    getPromotionsUseCase()
      .subscribeOn(rxSchedulers.io)
      .asAsyncToState(HomeState::promotionsModelAsync) { copy(promotionsModelAsync = it) }
      .doOnNext { promotionsModel ->
        if (promotionsModel.error == null) {
          setSeenPromotionsUseCase(promotionsModel.promotions, promotionsModel.wallet.address)
        }
      }
      .repeatableScopedSubscribe(PromotionsState::promotionsModelAsync.name) { e ->
        e.printStackTrace()
      }
  }

  sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val transactions: Map<String, List<TransactionModel>>) : UiState()
  }
}