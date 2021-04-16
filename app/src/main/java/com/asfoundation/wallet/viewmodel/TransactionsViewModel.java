package com.asfoundation.wallet.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Pair;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.appcoins.wallet.gamification.repository.Levels;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.C;
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics;
import com.asfoundation.wallet.billing.analytics.WalletsEventSender;
import com.asfoundation.wallet.entity.Balance;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.asfoundation.wallet.entity.GlobalBalance;
import com.asfoundation.wallet.entity.NetworkInfo;
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
import java.util.concurrent.TimeUnit;

public class TransactionsViewModel extends BaseViewModel {
  private static final long UPDATE_INTERVAL = 30 * DateUtils.SECOND_IN_MILLIS;
  private static final BigDecimal MINUS_ONE = new BigDecimal("-1");
  private final MutableLiveData<NetworkInfo> defaultNetwork = new MutableLiveData<>();
  private final MutableLiveData<Wallet> defaultWallet = new MutableLiveData<>();
  private final MutableLiveData<TransactionsModel> transactionsModel = new MutableLiveData<>();
  private final MutableLiveData<CardNotification> dismissNotification = new MutableLiveData<>();
  private final MutableLiveData<Boolean> showNotification = new MutableLiveData<>();
  private final MutableLiveData<GlobalBalance> defaultWalletBalance = new MutableLiveData<>();
  private final MutableLiveData<Double> gamificationMaxBonus = new MutableLiveData<>();
  private final MutableLiveData<Double> fetchTransactionsError = new MutableLiveData<>();
  private final MutableLiveData<Boolean> unreadMessages = new MutableLiveData<>();
  private final MutableLiveData<String> shareApp = new MutableLiveData<>();
  private final MutableLiveData<Boolean> showPromotionTooltip = new MutableLiveData<>();
  private final MutableLiveData<Boolean> showFingerprintTooltip = new MutableLiveData<>();
  private final MutableLiveData<Integer> experimentAssignment = new MutableLiveData<>();
  private final SingleLiveEvent<Boolean> showRateUsDialog = new SingleLiveEvent<>();
  private final BehaviorSubject<Boolean> refreshData = BehaviorSubject.createDefault(false);
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
    handleBalanceWalletsExperiment();
    handleTopUpClicks();
    handleUnreadConversationCount();
    handlePromotionTooltipVisibility();
    handlePromotionUpdateNotification();
    handleRateUsDialogVisibility();
    handleConversationCount();
    handleWalletData();
  }

  public void updateData() {
    refreshData.onNext(true);
  }

  public void stopRefreshingData() {
    refreshData.onNext(false);
  }

  public void refreshTransactions(boolean shouldShowProgress) {
    disposables.add(
        updateTransactions(shouldShowProgress, defaultWallet.getValue()).subscribe(() -> {
        }, this::onError));
  }

  /**
   * Spine stream responsible for keeping wallet data up-to-date.
   *
   * If {@link #refreshData} is true, we retrieve the latest network and active wallet, we post
   * its values, we check if the wallet changed and refresh the rest of the data.
   *
   * We use switchMap to disregard previous subscriptions every time we need to refresh data.
   *
   * @see #refreshTransactionsAndBalance(boolean, Wallet).
   */
  private void handleWalletData() {
    disposables.add(observeRefreshData().switchMap(__ -> observeNetworkAndWallet())
        .doOnNext(walletNetworkModel -> {
          if (walletNetworkModel.isNewWallet()) {
            defaultWallet.postValue(walletNetworkModel.getWallet());
          }
          defaultNetwork.postValue(walletNetworkModel.getNetworkInfo());
        })
        .switchMapCompletableDelayError(this::updateWalletData)
        .subscribe(() -> {
        }, this::onError));
  }

  private Observable<TransactionsWalletModel> observeNetworkAndWallet() {
    return Observable.combineLatest(transactionViewInteractor.findNetwork()
        .toObservable(), transactionViewInteractor.observeWallet(), (networkInfo, wallet) -> {
      Wallet previousWallet = defaultWallet.getValue();
      boolean isNewWallet = previousWallet == null || !previousWallet.sameAddress(wallet.address);
      return new TransactionsWalletModel(networkInfo, wallet, isNewWallet);
    });
  }

  private Completable updateWalletData(TransactionsWalletModel model) {
    return Completable.mergeArrayDelayError(
        refreshTransactionsAndBalance(model.isNewWallet(), model.getWallet()),
        updateRegisterUser(model.getWallet()));
  }

  /**
   * Responsible for continuously refreshing transactions and balance. It refreshes every
   * {@link UPDATE_INTERVAL} seconds. Note that if refreshData is false, it won't refresh it.
   */
  private Completable refreshTransactionsAndBalance(boolean walletChanged, Wallet wallet) {
    return Observable.interval(0, UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
        .flatMap(__ -> observeRefreshData())
        .switchMapCompletable(__ -> Completable.mergeArrayDelayError(updateBalance(),
            updateTransactions(walletChanged, wallet))
            .subscribeOn(networkScheduler))
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

  public LiveData<NetworkInfo> defaultNetwork() {
    return defaultNetwork;
  }

  public LiveData<Wallet> defaultWallet() {
    return defaultWallet;
  }

  public LiveData<TransactionsModel> transactionsModel() {
    return transactionsModel;
  }

  public LiveData<CardNotification> dismissNotification() {
    return dismissNotification;
  }

  public MutableLiveData<GlobalBalance> getDefaultWalletBalance() {
    return defaultWalletBalance;
  }

  public MutableLiveData<Boolean> shouldShowPromotionsTooltip() {
    return showPromotionTooltip;
  }

  public MutableLiveData<Integer> balanceWalletsExperimentAssignment() {
    return experimentAssignment;
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

  private void handleBalanceWalletsExperiment() {
    disposables.add(transactionViewInteractor.getBalanceWalletsExperiment()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess(assignment -> {
          @StringRes int bottomNavigationItemName =
              transactionViewInteractor.mapConfiguration(assignment);
          analytics.sendAbTestImpressionEvent(assignment);
          experimentAssignment.postValue(bottomNavigationItemName);
        })
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  private void handleRateUsDialogVisibility() {
    disposables.add(observeRefreshData().switchMap(
        __ -> transactionViewInteractor.shouldOpenRatingDialog()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess(showRateUsDialog::setValue)
            .toObservable())
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  public void handleFingerprintTooltipVisibility(String packageName) {
    disposables.add(transactionViewInteractor.shouldShowFingerprintTooltip(packageName)
        .doOnSuccess(showFingerprintTooltip::postValue)
        .subscribe(__ -> {
        }, Throwable::printStackTrace));
  }

  private void handlePromotionTooltipVisibility() {
    disposables.add(observeRefreshData().switchMap(
        __ -> transactionViewInteractor.hasSeenPromotionTooltip()
            .doOnSuccess(hasBeen -> {
              Boolean shouldShowCachedValue = showPromotionTooltip.getValue();
              boolean shouldShow =
                  !hasBeen && (shouldShowCachedValue == null || shouldShowCachedValue);
              showPromotionTooltip.postValue(shouldShow);
            })
            .toObservable())
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

  private Completable publishMaxBonus() {
    if (fetchTransactionsError.getValue() != null) {
      return Completable.fromAction(
          () -> fetchTransactionsError.postValue(fetchTransactionsError.getValue()));
    }
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
        .doOnSuccess(fetchTransactionsError::postValue)
        .ignoreElement();
  }

  private Completable updateTransactions(boolean shouldShowProgress, Wallet wallet) {
    return Completable.fromAction(() -> progress.postValue(shouldShowProgress))
        .andThen(transactionViewInteractor.fetchTransactions(wallet))
        .flatMapSingle(transactions -> transactionViewInteractor.getCardNotifications()
            .subscribeOn(networkScheduler)
            .onErrorReturnItem(Collections.emptyList())
            .flatMap(notifications -> applications.getApps()
                .onErrorReturnItem(Collections.emptyList())
                .map(applications -> new TransactionsModel(transactions, notifications,
                    applications))))
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .flatMapCompletable(transactionsModel -> publishMaxBonus().observeOn(viewScheduler)
            .andThen(onTransactionModel(transactionsModel))
            .andThen(Completable.fromAction(this::onTransactionsFetchCompleted)))
        .onErrorResumeNext(throwable -> publishMaxBonus())
        .observeOn(viewScheduler)
        .doAfterTerminate(transactionViewInteractor::stopTransactionFetch);
  }

  private Completable updateBalance() {
    return Observable.zip(getAppcBalance(), getCreditsBalance(), getEthereumBalance(),
        this::updateWalletValue)
        .firstOrError()
        .ignoreElement();
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
            tokenBalance.second.getSymbol(), fiatValue, shouldShow(tokenBalance, 0.01),
            shouldShow(creditsBalance, 0.01), shouldShow(ethereumBalance, 0.0001));
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
    return transactionViewInteractor.getAppcBalance();
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

  private Completable onTransactionModel(TransactionsModel transactionsModel) {
    return Completable.fromAction(() -> {
      transactionsModel.getTransactions();
      hasTransactions = !transactionsModel.getTransactions()
          .isEmpty() || hasTransactions;
      this.transactionsModel.setValue(transactionsModel);
      Boolean last = progress.getValue();
      if (transactionsModel.getTransactions()
          .size() > 0 && last != null && last) {
        progress.postValue(true);
      }
      transactionViewInteractor.updateTransactionsNumber(transactionsModel.getTransactions());
    });
  }

  private void onTransactionsFetchCompleted() {
    progress.postValue(false);
    if (!hasTransactions) {
      error.postValue(new ErrorEnvelope(C.ErrorCode.EMPTY_COLLECTION, "empty collection"));
    }
  }

  public void showSettings(Context context) {
    transactionViewNavigator.openSettings(context, false);
  }

  public void showSend(Context context) {
    transactionViewNavigator.openSendView(context);
  }

  public void showDetails(Context context, Transaction transaction) {
    transactionViewNavigator.openTransactionsDetailView(context, transaction);
  }

  public void showMyAddress(Context context) {
    transactionViewNavigator.openMyAddressView(context, defaultWallet.getValue());
  }

  public void showTokens(Context context) {
    analytics.sendAbTestConversionEvent();
    transactionViewNavigator.openTokensView(context,
        transactionViewInteractor.getCachedExperiment());
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

  public MutableLiveData<Double> gamificationMaxBonus() {
    return gamificationMaxBonus;
  }

  public MutableLiveData<String> shareApp() {
    return shareApp;
  }

  public MutableLiveData<Double> onFetchTransactionsError() {
    return fetchTransactionsError;
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
        Wallet wallet = defaultWallet.getValue();
        if (wallet != null && wallet.address != null) {
          transactionViewNavigator.navigateToBackup(context, wallet.address);
          walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
              WalletsAnalytics.CONTEXT_CARD, WalletsAnalytics.STATUS_SUCCESS);
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
        .subscribe(() -> dismissNotification.postValue(cardNotification), this::onError));
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
