package com.asfoundation.wallet.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.appcoins.wallet.gamification.repository.Levels;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics;
import com.asfoundation.wallet.billing.analytics.WalletsEventSender;
import com.asfoundation.wallet.entity.Balance;
import com.asfoundation.wallet.entity.GlobalBalance;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.TransactionViewInteractor;
import com.asfoundation.wallet.navigator.TransactionViewNavigator;
import com.asfoundation.wallet.promotions.PromotionNotification;
import com.asfoundation.wallet.referrals.CardNotification;
import com.asfoundation.wallet.support.SupportInteractor;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.transactions.TransactionsAnalytics;
import com.asfoundation.wallet.ui.AppcoinsApps;
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication;
import com.asfoundation.wallet.ui.iab.FiatValue;
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel;
import com.asfoundation.wallet.ui.widget.holder.ApplicationClickAction;
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction;
import com.asfoundation.wallet.util.CurrencyFormatUtils;
import com.asfoundation.wallet.util.SingleLiveEvent;
import com.asfoundation.wallet.util.WalletCurrency;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TransactionsViewModel extends BaseViewModel {
  private static final long UPDATE_INTERVAL = 30 * DateUtils.SECOND_IN_MILLIS;
  private static final BigDecimal MINUS_ONE = new BigDecimal("-1");
  private final MutableLiveData<TransactionsWalletModel> defaultWalletModel =
      new MutableLiveData<>();
  private final MutableLiveData<Pair<TransactionsModel, TransactionsWalletModel>>
      transactionsModel = new MutableLiveData<>();
  private final MutableLiveData<Boolean> showNotification = new MutableLiveData<>();
  private final MutableLiveData<GlobalBalance> defaultWalletBalance = new MutableLiveData<>();
  private final MutableLiveData<Boolean> unreadMessages = new MutableLiveData<>();
  private final MutableLiveData<String> shareApp = new MutableLiveData<>();
  private final MutableLiveData<Boolean> showPromotionTooltip = new MutableLiveData<>();
  private final MutableLiveData<Boolean> showFingerprintTooltip = new MutableLiveData<>();
  private final MutableLiveData<Boolean> showVipBadge = new MutableLiveData<>();
  private final SingleLiveEvent<Boolean> showRateUsDialog = new SingleLiveEvent<>();
  private final BehaviorSubject<Boolean> refreshData = BehaviorSubject.createDefault(true);
  private final BehaviorSubject<Boolean> refreshCardNotifications =
      BehaviorSubject.createDefault(true);
  private final AppcoinsApps applications;
  private final TransactionsAnalytics analytics;
  private final TransactionViewNavigator transactionViewNavigator;
  private final TransactionViewInteractor transactionViewInteractor;
  private final SupportInteractor supportInteractor;
  private final WalletsEventSender walletsEventSender;
  private final PublishSubject<Context> topUpClicks = PublishSubject.create();
  private final CurrencyFormatUtils formatter;
  private final Scheduler viewScheduler;
  private final Scheduler networkScheduler;
  private final CompositeDisposable disposables;
  private boolean hasTransactions = false;

  TransactionsViewModel(AppcoinsApps applications, TransactionsAnalytics analytics,
      TransactionViewNavigator transactionViewNavigator,
      TransactionViewInteractor transactionViewInteractor, SupportInteractor supportInteractor,
      WalletsEventSender walletsEventSender, CurrencyFormatUtils formatter, Scheduler viewScheduler,
      Scheduler networkScheduler) {
    this.applications = applications;
    this.analytics = analytics;
    this.transactionViewNavigator = transactionViewNavigator;
    this.transactionViewInteractor = transactionViewInteractor;
    this.supportInteractor = supportInteractor;
    this.walletsEventSender = walletsEventSender;
    this.formatter = formatter;
    this.viewScheduler = viewScheduler;
    this.networkScheduler = networkScheduler;
    this.disposables = new CompositeDisposable();
    init();
  }

  private void init() {
    progress.postValue(true);
    handleTopUpClicks();
    handleUnreadConversationCount();
    handlePromotionTooltipVisibility();
    handlePromotionUpdateNotification();
    handleRateUsDialogVisibility();
    handleConversationCount();
    handleWalletData();
    handleFingerprintTooltipVisibility();
    verifyUserLevel();
  }

  public void updateData() {
    refreshData.onNext(true);
  }

  public void stopRefreshingData() {
    refreshData.onNext(false);
  }

  /**
   * Spine stream responsible for keeping wallet data up-to-date.
   *
   * If {@link #refreshData} is true, we retrieve the latest network and active wallet, we post
   * its values, we check if the wallet changed and refresh the rest of the data.
   *
   * We use switchMap to disregard previous subscriptions every time we need to refresh data.
   *
   * @see #refreshTransactionsAndBalance(TransactionsWalletModel model) .
   */
  private void handleWalletData() {
    disposables.add(observeRefreshData().switchMap(__ -> observeNetworkAndWallet())
        .doOnNext(walletNetworkModel -> {
          if (walletNetworkModel.isNewWallet()) {
            defaultWalletModel.postValue(walletNetworkModel);
          }
        })
        .switchMapCompletable(this::updateWalletData)
        .subscribe(() -> {
        }, this::onError));
  }

  private Observable<TransactionsWalletModel> observeNetworkAndWallet() {
    return Observable.combineLatest(transactionViewInteractor.findNetwork()
        .toObservable(), transactionViewInteractor.observeWallet(), (networkInfo, wallet) -> {
      TransactionsWalletModel previousModel = defaultWalletModel.getValue();
      boolean isNewWallet = previousModel == null || !previousModel.getWallet()
          .sameAddress(wallet.address);
      return new TransactionsWalletModel(networkInfo, wallet, isNewWallet);
    });
  }

  private Completable updateWalletData(TransactionsWalletModel model) {
    return Completable.mergeArray(refreshTransactionsAndBalance(model),
        updateRegisterUser(model.getWallet()));
  }

  private Completable refreshTransactionsAndBalance(TransactionsWalletModel model) {
    return Completable.mergeArray(updateBalance(),
        updateTransactions(model).subscribeOn(networkScheduler))
        .subscribeOn(networkScheduler);
  }

  private Observable<Boolean> observeRefreshData() {
    return refreshData.filter(refreshData -> refreshData);
  }

  @Override protected void onCleared() {
    super.onCleared();
    hasTransactions = false;
    disposables.dispose();
  }

  public LiveData<TransactionsWalletModel> defaultWalletModel() {
    return defaultWalletModel;
  }

  public LiveData<Pair<TransactionsModel, TransactionsWalletModel>> transactionsModel() {
    return transactionsModel;
  }

  public MutableLiveData<GlobalBalance> getDefaultWalletBalance() {
    return defaultWalletBalance;
  }

  public MutableLiveData<Boolean> shouldShowPromotionsTooltip() {
    return showPromotionTooltip;
  }

  public LiveData<Boolean> shouldShowRateUsDialog() {
    return showRateUsDialog;
  }

  private void handlePromotionUpdateNotification() {
    disposables.add(observeRefreshData().switchMap(
        __ -> transactionViewInteractor.hasPromotionUpdate()
            .doOnSuccess(showNotification::postValue)
            .subscribeOn(networkScheduler)
            .toObservable())

        .subscribe(__ -> {
        }, this::onError));
  }

  private Completable updateRegisterUser(Wallet wallet) {
    return transactionViewInteractor.getUserLevel()
        .subscribeOn(networkScheduler)
        .map(userLevel -> {
          registerSupportUser(userLevel, wallet.address);
          return true;
        })
        .ignoreElement()
        .subscribeOn(networkScheduler);
  }



  private void handleRateUsDialogVisibility() {
    disposables.add(transactionViewInteractor.shouldOpenRatingDialog()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess(showRateUsDialog::setValue)
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  public void handleFingerprintTooltipVisibility() {
    disposables.add(
        transactionViewInteractor.shouldShowFingerprintTooltip(BuildConfig.APPLICATION_ID)
            .doOnSuccess(showFingerprintTooltip::postValue)
            .toObservable()
            .subscribe(__ -> {
            }, Throwable::printStackTrace));
  }

  private void handlePromotionTooltipVisibility() {
    disposables.add(transactionViewInteractor.hasSeenPromotionTooltip()
        .doOnSuccess(hasBeen -> {
          Boolean shouldShowCachedValue = showPromotionTooltip.getValue();
          boolean shouldShow = !hasBeen && (shouldShowCachedValue == null || shouldShowCachedValue);
          showPromotionTooltip.postValue(shouldShow);
        })
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  public void handleUnreadConversationCount() {
    disposables.add(observeRefreshData().switchMap(
        __ -> supportInteractor.getUnreadConversationCountEvents()
            .subscribeOn(viewScheduler)
            .doOnNext(this::updateIntercomAnimation))
        .subscribe());
  }

  public void handleConversationCount() {
    disposables.add(observeRefreshData().switchMap(
        __ -> supportInteractor.getUnreadConversationCount()
            .subscribeOn(viewScheduler)
            .doOnSuccess(this::updateIntercomAnimation)
            .toObservable())
        .subscribe());
  }

  private void updateIntercomAnimation(Integer count) {
    unreadMessages.setValue(count != null && count != 0);
  }

  private Completable updateTransactions(TransactionsWalletModel walletModel) {
    if (walletModel == null) return Completable.complete();

    return Completable.fromObservable(
        Observable.combineLatest(getTransactions(walletModel.getWallet()), getCardNotifications(),
            getAppcoinsApps(), getMaxBonus(), this::createTransactionsModel)
            .doOnNext(transactionsModel -> transactionViewInteractor.updateTransactionsNumber(
                transactionsModel.getTransactions()))
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnNext(transactionsModel -> onTransactionModel(transactionsModel, walletModel))
            .map(__ -> walletModel));
  }

  private TransactionsModel createTransactionsModel(List<Transaction> transactions,
      List<CardNotification> notifications, List<AppcoinsApplication> apps, Double maxBonus) {
    return new TransactionsModel(transactions, notifications, apps, maxBonus);
  }

  /**
   * Transactions are refreshed every {@link #UPDATE_INTERVAL} seconds, and stops while
   * {@link #refreshData} is false
   */
  private Observable<List<Transaction>> getTransactions(Wallet wallet) {
    return Observable.interval(0, UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
        .flatMap(__ -> observeRefreshData())
        .switchMap(__ -> transactionViewInteractor.fetchTransactions(wallet))
        .subscribeOn(networkScheduler)
        .onErrorReturnItem(Collections.emptyList())
        .doAfterTerminate(transactionViewInteractor::stopTransactionFetch);
  }

  private Observable<List<CardNotification>> getCardNotifications() {
    return refreshCardNotifications.flatMapSingle(
        __ -> transactionViewInteractor.getCardNotifications())
        .subscribeOn(networkScheduler)
        .onErrorReturnItem(Collections.emptyList());
  }

  private Observable<List<AppcoinsApplication>> getAppcoinsApps() {
    return applications.getApps()
        .subscribeOn(networkScheduler)
        .onErrorReturnItem(Collections.emptyList())
        .toObservable();
  }

  private Observable<Double> getMaxBonus() {
    return transactionViewInteractor.getLevels()
        .subscribeOn(networkScheduler)
        .flatMap(levels -> {
          if (levels.getStatus()
              .equals(Levels.Status.OK)) {
            return Single.just(levels.getList()
                .get(levels.getList()
                    .size() - 1)
                .getBonus());
          }
          return Single.error(new IllegalStateException(levels.getStatus()
              .name()));
        })
        .toObservable();
  }

  /**
   * Balance is refreshed every {@link #UPDATE_INTERVAL} seconds, and stops while
   * {@link #refreshData} is false
   */
  private Completable updateBalance() {
    return Completable.fromObservable(Observable.interval(0, UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
        .flatMap(__ -> observeRefreshData())
        .switchMap(__ -> Observable.zip(getAppcBalance(), getCreditsBalance(), getEthereumBalance(),
            this::updateWalletValue)));
  }

  private GlobalBalance updateWalletValue(Pair<Balance, FiatValue> tokenBalance,
      Pair<Balance, FiatValue> creditsBalance, Pair<Balance, FiatValue> ethereumBalance) {
    String fiatValue = "";
    BigDecimal sumFiat = sumFiat(tokenBalance.second.getAmount(), creditsBalance.second.getAmount(),
        ethereumBalance.second.getAmount());
    if (sumFiat.compareTo(MINUS_ONE) > 0) {
      fiatValue = formatter.formatCurrency(sumFiat, WalletCurrency.FIAT);
    }
    GlobalBalance currentGlobalBalance = defaultWalletBalance.getValue();
    GlobalBalance newGlobalBalance =
        new GlobalBalance(tokenBalance.first, creditsBalance.first, ethereumBalance.first,
            tokenBalance.second.getSymbol(), tokenBalance.second.getCurrency(), fiatValue,
            shouldShow(tokenBalance, 0.01), shouldShow(creditsBalance, 0.01),
            shouldShow(ethereumBalance, 0.0001));
    if (currentGlobalBalance != null) {
      if (!currentGlobalBalance.equals(newGlobalBalance)) {
        defaultWalletBalance.postValue(newGlobalBalance);
      }
    } else {
      defaultWalletBalance.postValue(newGlobalBalance);
    }
    return newGlobalBalance;
  }

  private Observable<Pair<Balance, FiatValue>> getAppcBalance() {
    return transactionViewInteractor.getAppcBalance()
        .filter(pair -> !pair.second.getSymbol()
            .isEmpty());
  }

  private Observable<Pair<Balance, FiatValue>> getEthereumBalance() {
    return transactionViewInteractor.getEthereumBalance();
  }

  private Observable<Pair<Balance, FiatValue>> getCreditsBalance() {
    return transactionViewInteractor.getCreditsBalance();
  }

  private boolean shouldShow(Pair<Balance, FiatValue> balance, Double threshold) {
    return balance.first.getStringValue()
        .length() > 0
        && Double.parseDouble(balance.first.getStringValue()) >= threshold
        && (balance.second.getAmount()
        .compareTo(MINUS_ONE) > 0)
        && balance.second.getAmount()
        .doubleValue() >= threshold;
  }

  private BigDecimal sumFiat(BigDecimal appcoinsFiatValue, BigDecimal creditsFiatValue,
      BigDecimal etherFiatValue) {
    BigDecimal fiatSum = MINUS_ONE;
    if (appcoinsFiatValue.compareTo(MINUS_ONE) > 0) {
      fiatSum = appcoinsFiatValue;
    }

    if (creditsFiatValue.compareTo(MINUS_ONE) > 0) {
      if (fiatSum.compareTo(MINUS_ONE) > 0) {
        fiatSum = fiatSum.add(creditsFiatValue);
      } else {
        fiatSum = creditsFiatValue;
      }
    }

    if (etherFiatValue.compareTo(MINUS_ONE) > 0) {
      if (fiatSum.compareTo(MINUS_ONE) > 0) {
        fiatSum = fiatSum.add(etherFiatValue);
      } else {
        fiatSum = etherFiatValue;
      }
    }
    return fiatSum;
  }

  private void onTransactionModel(TransactionsModel transactionsModel,
      TransactionsWalletModel walletModel) {
    hasTransactions = !transactionsModel.getTransactions()
        .isEmpty() || hasTransactions;
    this.transactionsModel.setValue(new Pair<>(transactionsModel, walletModel));
  }

  public void showSettings(Context context) {
    transactionViewNavigator.openSettings(context, false);
  }

  public void goToVipLink(Context context) {
    analytics.sendAction("vip_badge");
    Uri uri = Uri.parse(BuildConfig.VIP_PROGRAM_BADGE_URL);
    transactionViewNavigator.navigateToBrowser(context, uri);
  }

  public LiveData<Boolean> shouldShowVipBadge() {
    return showVipBadge;
  }

  public void verifyUserLevel() {
    disposables.add(transactionViewInteractor.findWallet()
        .subscribeOn(networkScheduler)
        .flatMap(wallet -> transactionViewInteractor.getUserLevel()
            .subscribeOn(networkScheduler)
            .doOnSuccess(userLevel -> showVipBadge.postValue(userLevel == 9 || userLevel == 10)))
        .subscribe(wallet -> {
        }, this::onError));
  }

  public void showSend(Context context) {
    transactionViewNavigator.openSendView(context);
  }

  public void showDetails(Context context, Transaction transaction) {
    transactionViewNavigator.openTransactionsDetailView(context, transaction,
        defaultWalletBalance.getValue()
            .getFiatCurrency());
  }

  public void showMyAddress(Context context) {
    TransactionsWalletModel model = defaultWalletModel.getValue();
    if (model != null) {
      transactionViewNavigator.openMyAddressView(context, model.getWallet());
    }
  }

  public void showTokens(Context context) {
    transactionViewNavigator.openMyWalletsView(context);
  }

  public void onAppClick(AppcoinsApplication appcoinsApplication,
      ApplicationClickAction applicationClickAction, Context context) {
    String url = "https://" + appcoinsApplication.getUniqueName() + ".en.aptoide.com/";
    switch (applicationClickAction) {
      case SHARE:
        shareApp.setValue(url);
        break;
      case CLICK:
      default:
        transactionViewNavigator.navigateToBrowser(context, Uri.parse(url));
        analytics.openApp(appcoinsApplication.getUniqueName(),
            appcoinsApplication.getPackageName());
    }
  }

  public void showTopApps(Context context) {
    transactionViewNavigator.navigateToBrowser(context,
        Uri.parse(BuildConfig.APTOIDE_TOP_APPS_URL));
  }

  public MutableLiveData<Boolean> shouldShowPromotionsNotification() {
    return showNotification;
  }

  public void showTopUp(Context context) {
    topUpClicks.onNext(context);
  }

  public MutableLiveData<String> shareApp() {
    return shareApp;
  }

  public MutableLiveData<Boolean> getUnreadMessages() {
    return unreadMessages;
  }

  public void navigateToPromotions(Context context) {
    transactionViewNavigator.openPromotions(context);
  }

  public void onNotificationClick(CardNotification cardNotification,
      CardNotificationAction cardNotificationAction, Context context) {
    switch (cardNotificationAction) {
      case DISMISS:
        dismissNotification(cardNotification);
        break;
      case DISCOVER:
        transactionViewNavigator.navigateToBrowser(context,
            Uri.parse(BuildConfig.APTOIDE_TOP_APPS_URL));
        break;
      case UPDATE:
        transactionViewNavigator.openIntent(context,
            transactionViewInteractor.retrieveUpdateIntent());
        dismissNotification(cardNotification);
        break;
      case BACKUP:
        TransactionsWalletModel model = defaultWalletModel.getValue();
        if (model != null) {
          Wallet wallet = model.getWallet();
          if (wallet.address != null) {
            transactionViewNavigator.navigateToBackup(context, wallet.address);
            walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
                WalletsAnalytics.CONTEXT_CARD, WalletsAnalytics.STATUS_SUCCESS);
          }
        }
        break;
      case DETAILS_URL:
        if (cardNotification instanceof PromotionNotification) {
          String url = ((PromotionNotification) cardNotification).getDetailsLink();
          transactionViewNavigator.navigateToBrowser(context, Uri.parse(url));
        }
        break;
      case NONE:
        break;
    }
  }

  private void dismissNotification(CardNotification cardNotification) {
    disposables.add(transactionViewInteractor.dismissNotification(cardNotification)
        .subscribeOn(viewScheduler)
        .doOnComplete(() -> refreshCardNotifications.onNext(true))
        .subscribe(() -> {
        }, this::onError));
  }

  public void showSupportScreen(boolean fromNotification) {
    if (fromNotification) {
      supportInteractor.displayConversationListOrChat();
    } else {
      supportInteractor.displayChatScreen();
    }
  }

  private void registerSupportUser(Integer level, String walletAddress) {
    supportInteractor.registerUser(level, walletAddress);
  }

  private void handleTopUpClicks() {
    disposables.add(topUpClicks.throttleFirst(1, TimeUnit.SECONDS)
        .doOnNext(transactionViewNavigator::openTopUp)
        .subscribe());
  }

  public void clearShareApp() {
    shareApp.setValue(null);
  }

  public MutableLiveData<Boolean> shouldShowFingerprintTooltip() {
    return showFingerprintTooltip;
  }

  public void increaseTimesInHome() {
    transactionViewInteractor.increaseTimesOnHome();
  }

  public void onTurnFingerprintOnClick(Context context) {
    transactionViewNavigator.openSettings(context, true);
    transactionViewInteractor.setSeenFingerprintTooltip();
  }

  public void onFingerprintDismissed() {
    transactionViewInteractor.setSeenFingerprintTooltip();
  }

  public void onPromotionsShown() {
    showPromotionTooltip.postValue(false);
  }

  public void onFingerprintTooltipShown() {
    showFingerprintTooltip.postValue(false);
  }
}