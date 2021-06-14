package com.asfoundation.wallet.home

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.util.Pair
import com.appcoins.wallet.gamification.repository.Levels
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.GlobalBalance
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.home.usecases.*
import com.asfoundation.wallet.promotions.PromotionNotification
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.AppcoinsApps
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel
import com.asfoundation.wallet.ui.widget.holder.ApplicationClickAction
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.viewmodel.TransactionsWalletModel
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

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
  object ShowFingerprintTooltip : HomeSideEffect()
}

data class HomeState(
    val transactionsModelAsync: Async<TransactionsModel> = Async.Uninitialized,
    val defaultWalletBalanceAsync: Async<GlobalBalance> = Async.Uninitialized,
    val showVipBadge: Boolean = false,
    val unreadMessages: Boolean = false) :
    ViewState

class HomeViewModel(private val applications: AppcoinsApps,
                    private val analytics: HomeAnalytics,
                    private val shouldOpenRatingDialogUseCase: ShouldOpenRatingDialogUseCase,
                    private val updateTransactionsNumberUseCase: UpdateTransactionsNumberUseCase,
                    private val findNetworkInfoUseCase: FindNetworkInfoUseCase,
                    private val fetchTransactionsUseCase: FetchTransactionsUseCase,
                    private val stopFetchTransactionsUseCase: StopFetchTransactionsUseCase,
                    private val findDefaultWalletUseCase: FindDefaultWalletUseCase,
                    private val observeDefaultWalletUseCase: ObserveDefaultWalletUseCase,
                    private val dismissCardNotificationUseCase: DismissCardNotificationUseCase,
                    private val buildAutoUpdateIntentUseCase: BuildAutoUpdateIntentUseCase,
                    private val shouldShowFingerprintTooltipUseCase: ShouldShowFingerprintTooltipUseCase,
                    private val setSeenFingerprintTooltipUseCase: SetSeenFingerprintTooltipUseCase,
                    private val getLevelsUseCase: GetLevelsUseCase,
                    private val getUserLevelUseCase: GetUserLevelUseCase,
                    private val getAppcBalanceUseCase: GetAppcBalanceUseCase,
                    private val getEthBalanceUseCase: GetEthBalanceUseCase,
                    private val getCreditsBalanceUseCase: GetCreditsBalanceUseCase,
                    private val getCardNotificationsUseCase: GetCardNotificationsUseCase,
                    private val supportInteractor: SupportInteractor,
                    private val walletsEventSender: WalletsEventSender,
                    private val formatter: CurrencyFormatUtils,
                    private val viewScheduler: Scheduler,
                    private val networkScheduler: Scheduler) :
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
        .switchMapCompletable { this.updateWalletData(it) }
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

  private fun updateWalletData(model: TransactionsWalletModel): Completable {
    return Completable.mergeArray(refreshTransactionsAndBalance(model),
        updateRegisterUser(model.wallet))
  }

  private fun updateRegisterUser(wallet: Wallet): Completable? {
    return getUserLevelUseCase()
        .subscribeOn(networkScheduler)
        .map { userLevel ->
          registerSupportUser(userLevel, wallet.address)
          true
        }
        .ignoreElement()
        .subscribeOn(networkScheduler)
  }

  private fun registerSupportUser(level: Int, walletAddress: String) {
    supportInteractor.registerUser(level, walletAddress)
  }

  private fun refreshTransactionsAndBalance(model: TransactionsWalletModel): Completable {
    return Completable.mergeArray(updateBalance(),
        updateTransactions(model).subscribeOn(networkScheduler))
        .subscribeOn(networkScheduler)
  }

  /**
   * Balance is refreshed every [.UPDATE_INTERVAL] seconds, and stops while
   * [.refreshData] is false
   */
  private fun updateBalance(): Completable {
    return Completable.fromObservable(
        Observable.interval(0, UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
            .flatMap { observeRefreshData() }
            .switchMap {
              Observable.zip(
                  getAppcBalance(), getCreditsBalance(), getEthereumBalance(),
                  { tokenBalance, creditsBalance, ethereumBalance ->
                    this.mapWalletValue(tokenBalance, creditsBalance, ethereumBalance)
                  })
                  .asAsyncToState(HomeState::defaultWalletBalanceAsync) {
                    copy(defaultWalletBalanceAsync = it)
                  }
            })
  }

  private fun getAppcBalance(): Observable<Pair<Balance, FiatValue>> {
    return getAppcBalanceUseCase()
        .filter { pair: Pair<Balance, FiatValue> ->
          pair.second.symbol.isNotEmpty()
        }
  }

  private fun getEthereumBalance(): Observable<Pair<Balance, FiatValue>> {
    return getEthBalanceUseCase()
  }

  private fun getCreditsBalance(): Observable<Pair<Balance, FiatValue>> {
    return getCreditsBalanceUseCase()
  }

  private fun mapWalletValue(tokenBalance: Pair<Balance, FiatValue>,
                             creditsBalance: Pair<Balance, FiatValue>,
                             ethereumBalance: Pair<Balance, FiatValue>): GlobalBalance {
    var fiatValue = ""
    val sumFiat: BigDecimal = sumFiat(tokenBalance.second.amount, creditsBalance.second.amount,
        ethereumBalance.second.amount)
    if (sumFiat > MINUS_ONE) {
      fiatValue = formatter.formatCurrency(sumFiat, WalletCurrency.FIAT)
    }
    return GlobalBalance(tokenBalance.first, creditsBalance.first, ethereumBalance.first,
        tokenBalance.second.symbol, tokenBalance.second.currency, fiatValue,
        shouldShow(tokenBalance, 0.01), shouldShow(creditsBalance, 0.01),
        shouldShow(ethereumBalance, 0.0001))
  }

  private fun shouldShow(balance: Pair<Balance, FiatValue>, threshold: Double): Boolean {
    return (balance.first.getStringValue()
        .isNotEmpty() && balance.first.getStringValue()
        .toDouble() >= threshold && balance.second.amount > MINUS_ONE
        && balance.second.amount
        .toDouble() >= threshold)
  }

  private fun sumFiat(appcoinsFiatValue: BigDecimal, creditsFiatValue: BigDecimal,
                      etherFiatValue: BigDecimal): BigDecimal {
    var fiatSum = MINUS_ONE
    if (appcoinsFiatValue > MINUS_ONE) {
      fiatSum = appcoinsFiatValue
    }
    if (creditsFiatValue > MINUS_ONE) {
      fiatSum = if (fiatSum > MINUS_ONE) {
        fiatSum.add(creditsFiatValue)
      } else {
        creditsFiatValue
      }
    }
    if (etherFiatValue > MINUS_ONE) {
      fiatSum = if (fiatSum > MINUS_ONE) {
        fiatSum.add(etherFiatValue)
      } else {
        etherFiatValue
      }
    }
    return fiatSum
  }

  private fun updateTransactions(walletModel: TransactionsWalletModel?): Completable {
    if (walletModel == null) return Completable.complete()
    val retainValue = if (walletModel.isNewWallet) null else HomeState::transactionsModelAsync
    return Completable.fromObservable(
        Observable.combineLatest(getTransactions(walletModel.wallet), getCardNotifications(),
            getAppcoinsApps(), getMaxBonus(), observeNetworkAndWallet(),
            { transactions: List<Transaction>, notifications: List<CardNotification>, apps: List<AppcoinsApplication>, maxBonus: Double, transactionsWalletModel: TransactionsWalletModel ->
              createTransactionsModel(transactions, notifications, apps, maxBonus,
                  transactionsWalletModel)
            })
            .doOnNext { (transactions) ->
              updateTransactionsNumberUseCase(
                  transactions)
            }
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .asAsyncToState(retainValue) { copy(transactionsModelAsync = it) }
            .map { walletModel })
  }

  private fun createTransactionsModel(transactions: List<Transaction>,
                                      notifications: List<CardNotification>,
                                      apps: List<AppcoinsApplication>, maxBonus: Double,
                                      transactionsWalletModel: TransactionsWalletModel): TransactionsModel {
    return TransactionsModel(transactions, notifications, apps, maxBonus, transactionsWalletModel)
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
        .subscribeOn(networkScheduler)
        .onErrorReturnItem(emptyList())
        .doAfterTerminate { stopFetchTransactionsUseCase() }
  }

  private fun getCardNotifications(): Observable<List<CardNotification>> {
    return refreshCardNotifications.flatMapSingle { getCardNotificationsUseCase() }
        .subscribeOn(networkScheduler)
        .onErrorReturnItem(emptyList())
  }

  private fun getAppcoinsApps(): Observable<List<AppcoinsApplication>> {
    return applications.apps
        .subscribeOn(networkScheduler)
        .onErrorReturnItem(emptyList())
        .toObservable()
  }

  private fun getMaxBonus(): Observable<Double> {
    return getLevelsUseCase()
        .subscribeOn(networkScheduler)
        .flatMap { (status, list) ->
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
        .subscribeOn(networkScheduler)
        .flatMap {
          getUserLevelUseCase()
              .subscribeOn(networkScheduler)
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
      supportInteractor.getUnreadConversationCountEvents()
          .subscribeOn(viewScheduler)
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
      supportInteractor.displayConversationListOrChat()
    } else {
      supportInteractor.displayChatScreen()
    }
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
        HomeSideEffect.NavigateToDetails(transaction,
            it.fiatCurrency)
      }
    }
  }

  fun onAppClick(appcoinsApplication: AppcoinsApplication,
                 applicationClickAction: ApplicationClickAction?) {
    val url = "https://" + appcoinsApplication.uniqueName + ".en.aptoide.com/"
    when (applicationClickAction) {
      ApplicationClickAction.SHARE -> sendSideEffect { HomeSideEffect.NavigateToShare(url) }
      ApplicationClickAction.CLICK -> {
        sendSideEffect { HomeSideEffect.NavigateToBrowser(Uri.parse(url)) }
        analytics.openApp(appcoinsApplication.uniqueName,
            appcoinsApplication.packageName)
      }
      else -> {
        sendSideEffect { HomeSideEffect.NavigateToBrowser(Uri.parse(url)) }
        analytics.openApp(appcoinsApplication.uniqueName,
            appcoinsApplication.packageName)
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
          HomeSideEffect.NavigateToIntent(buildAutoUpdateIntentUseCase())
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
      CardNotificationAction.DETAILS_URL -> if (cardNotification is PromotionNotification) {
        val url = cardNotification.detailsLink
        sendSideEffect { HomeSideEffect.NavigateToBrowser(Uri.parse(url)) }
      }
      CardNotificationAction.NONE -> {
      }
    }
  }

  private fun dismissNotification(cardNotification: CardNotification) {
    dismissCardNotificationUseCase(cardNotification)
        .subscribeOn(viewScheduler)
        .doOnComplete { refreshCardNotifications.onNext(true) }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
  }
}