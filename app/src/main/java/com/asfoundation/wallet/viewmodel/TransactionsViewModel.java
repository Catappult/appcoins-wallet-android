package com.asfoundation.wallet.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.appcoins.wallet.gamification.repository.Levels;
import com.asfoundation.wallet.C;
import com.asfoundation.wallet.di.TransactionViewInteract;
import com.asfoundation.wallet.di.TransactionViewNavigator;
import com.asfoundation.wallet.entity.Balance;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.asfoundation.wallet.entity.GlobalBalance;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.transactions.TransactionsAnalytics;
import com.asfoundation.wallet.ui.AppcoinsApps;
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication;
import com.asfoundation.wallet.ui.iab.FiatValue;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

public class TransactionsViewModel extends BaseViewModel {
  private static final long GET_BALANCE_INTERVAL = 10 * DateUtils.SECOND_IN_MILLIS;
  private static final long FETCH_TRANSACTIONS_INTERVAL = 12 * DateUtils.SECOND_IN_MILLIS;
  private static final int FIAT_SCALE = 2;
  private static final BigDecimal MINUS_ONE = new BigDecimal("-1");
  private final MutableLiveData<NetworkInfo> defaultNetwork = new MutableLiveData<>();
  private final MutableLiveData<Wallet> defaultWallet = new MutableLiveData<>();
  private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();
  private final MutableLiveData<Boolean> showNotification = new MutableLiveData<>();
  private final MutableLiveData<List<AppcoinsApplication>> appcoinsApplications =
      new MutableLiveData<>();
  private final MutableLiveData<GlobalBalance> defaultWalletBalance = new MutableLiveData<>();
  private final MutableLiveData<Double> gamificationMaxBonus = new MutableLiveData<>();
  private final MutableLiveData<Double> fetchTransactionsError = new MutableLiveData<>();
  private final CompositeDisposable disposables;
  private final DefaultTokenProvider defaultTokenProvider;
  private final AppcoinsApps applications;
  private final TransactionsAnalytics analytics;
  private final TransactionViewNavigator transactionViewNavigator;
  private final TransactionViewInteract transactionViewInteract;
  private Handler handler = new Handler();
  private final Runnable startGlobalBalanceTask = this::getGlobalBalance;
  private boolean hasTransactions = false;
  private Disposable fetchTransactionsDisposable;
  private final Runnable startFetchTransactionsTask = () -> this.fetchTransactions(false);

  TransactionsViewModel(DefaultTokenProvider defaultTokenProvider, AppcoinsApps applications,
      TransactionsAnalytics analytics, TransactionViewNavigator transactionViewNavigator,
      TransactionViewInteract transactionViewInteract) {
    this.defaultTokenProvider = defaultTokenProvider;
    this.applications = applications;
    this.analytics = analytics;
    this.transactionViewNavigator = transactionViewNavigator;
    this.transactionViewInteract = transactionViewInteract;
    this.disposables = new CompositeDisposable();
  }

  @Override protected void onCleared() {
    super.onCleared();
    hasTransactions = false;
    if (!disposables.isDisposed()) {
      disposables.dispose();
    }
    handler.removeCallbacks(startFetchTransactionsTask);
    handler.removeCallbacks(startGlobalBalanceTask);
  }

  public LiveData<NetworkInfo> defaultNetwork() {
    return defaultNetwork;
  }

  public LiveData<Wallet> defaultWallet() {
    return defaultWallet;
  }

  public LiveData<List<Transaction>> transactions() {
    return transactions;
  }

  public MutableLiveData<GlobalBalance> getDefaultWalletBalance() {
    return defaultWalletBalance;
  }

  public void prepare() {
    progress.postValue(true);
    disposables.add(transactionViewInteract.findNetwork()
        .subscribe(this::onDefaultNetwork, this::onError));
    disposables.add(transactionViewInteract.hasNewLevel()
        .subscribe(showNotification::postValue, this::onError));
  }

  private Completable publishMaxBonus() {
    if (fetchTransactionsError.getValue() != null) {
      return Completable.fromAction(
          () -> fetchTransactionsError.postValue(fetchTransactionsError.getValue()));
    }
    return transactionViewInteract.getLevels()
        .subscribeOn(Schedulers.io())
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
        .doOnSuccess(bonus -> fetchTransactionsError.postValue(bonus))
        .ignoreElement();
  }

  public void fetchTransactions(boolean shouldShowProgress) {
    handler.removeCallbacks(startFetchTransactionsTask);
    progress.postValue(shouldShowProgress);
    /*For specific address use: new Wallet("0x60f7a1cbc59470b74b1df20b133700ec381f15d3")*/
    if (fetchTransactionsDisposable != null && !fetchTransactionsDisposable.isDisposed()) {
      fetchTransactionsDisposable.dispose();
    }
    fetchTransactionsDisposable =
        transactionViewInteract.fetchTransactions(defaultWallet.getValue())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMapCompletable(
                transactions -> publishMaxBonus().observeOn(AndroidSchedulers.mainThread())
                    .andThen(onTransactions(transactions))
                    .andThen(Completable.fromAction(this::onTransactionsFetchCompleted)))
            .onErrorResumeNext(throwable -> publishMaxBonus())
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate(() -> transactionViewInteract.stopTransactionFetch())
            .subscribe(() -> {
            }, this::onError);
    disposables.add(fetchTransactionsDisposable);

    if (shouldShowProgress) {
      disposables.add(applications.getApps()
          .subscribeOn(Schedulers.io())
          .map(appcoinsApplications -> {
            Collections.shuffle(appcoinsApplications);
            return appcoinsApplications;
          })
          .observeOn(AndroidSchedulers.mainThread())
          .doOnSubscribe(disposable -> appcoinsApplications.postValue(Collections.emptyList()))
          .subscribe(appcoinsApplications::postValue, Throwable::printStackTrace));
    }
  }

  private void getGlobalBalance() {
    disposables.add(Observable.zip(getTokenBalance(), getCreditsBalance(), getEthereumBalance(),
        this::updateWalletValue)
        .subscribe(globalBalance -> {
          handler.removeCallbacks(startGlobalBalanceTask);
          handler.postDelayed(startGlobalBalanceTask, GET_BALANCE_INTERVAL);
        }, Throwable::printStackTrace));
  }

  private GlobalBalance updateWalletValue(Pair<Balance, FiatValue> tokenBalance,
      Pair<Balance, FiatValue> creditsBalance, Pair<Balance, FiatValue> ethereumBalance) {
    String fiatValue = "";
    BigDecimal sumFiat = sumFiat(tokenBalance.second.getAmount(), creditsBalance.second.getAmount(),
        ethereumBalance.second.getAmount());
    if (sumFiat.compareTo(MINUS_ONE) > 0) {
      fiatValue = sumFiat.setScale(FIAT_SCALE, RoundingMode.FLOOR)
          .toString();
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

  private Observable<Pair<Balance, FiatValue>> getTokenBalance() {
    return transactionViewInteract.getTokenBalance();
  }

  private Observable<Pair<Balance, FiatValue>> getEthereumBalance() {
    return transactionViewInteract.getEthereumBalance();
  }

  private Observable<Pair<Balance, FiatValue>> getCreditsBalance() {
    return transactionViewInteract.getCreditsBalance();
  }

  private boolean shouldShow(Pair<Balance, FiatValue> balance, Double threshold) {
    return balance.first.getStringValue()
        .length() > 0
        && Double.valueOf(balance.first.getStringValue()) >= threshold
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

  private void onDefaultNetwork(NetworkInfo networkInfo) {
    defaultNetwork.postValue(networkInfo);
    disposables.add(transactionViewInteract.findWallet()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::onDefaultWallet, this::onError));
  }

  private void onDefaultWallet(Wallet wallet) {
    defaultWallet.setValue(wallet);
    getGlobalBalance();
    fetchTransactions(true);
  }

  private Completable onTransactions(List<Transaction> transactions) {
    return Completable.fromAction(() -> {
      hasTransactions = (transactions != null && !transactions.isEmpty()) || hasTransactions;
      this.transactions.setValue(transactions);
      Boolean last = progress.getValue();
      if (transactions != null && transactions.size() > 0 && last != null && last) {
        progress.postValue(true);
      }
    });
  }

  private void onTransactionsFetchCompleted() {
    progress.postValue(false);
    if (!hasTransactions) {
      error.postValue(new ErrorEnvelope(C.ErrorCode.EMPTY_COLLECTION, "empty collection"));
    }
    handler.postDelayed(startFetchTransactionsTask, FETCH_TRANSACTIONS_INTERVAL);
  }

  public void showSettings(Context context) {
    transactionViewNavigator.openSettings(context);
  }

  public void showSend(Context context) {
    defaultTokenProvider.getDefaultToken()
        .doOnSuccess(defaultToken -> transactionViewNavigator.openSendView(context, defaultToken))
        .subscribe();
  }

  public void showDetails(Context context, Transaction transaction) {
    transactionViewNavigator.openTransactionsDetailView(context, transaction);
  }

  public void showMyAddress(Context context) {
    transactionViewNavigator.openMyAddressView(context, defaultWallet.getValue());
  }

  public void showTokens(Context context) {
    transactionViewNavigator.openTokensView(context, defaultWallet.getValue());
  }

  public void pause() {
    handler.removeCallbacks(startFetchTransactionsTask);
    handler.removeCallbacks(startGlobalBalanceTask);
  }

  public void openDeposit(Context context, Uri uri) {
    transactionViewNavigator.navigateToBrowser(context, uri);
  }

  public LiveData<List<AppcoinsApplication>> applications() {
    return appcoinsApplications;
  }

  public void onAppClick(AppcoinsApplication appcoinsApplication, Context context) {
    transactionViewNavigator.navigateToBrowser(context,
        Uri.parse("https://" + appcoinsApplication.getUniqueName() + ".en.aptoide.com/"));
    analytics.openApp(appcoinsApplication.getUniqueName(), appcoinsApplication.getPackageName());
  }

  public void showRewardsLevel(Context context) {
    transactionViewNavigator.openRewardsLevel(context);
  }

  public void showTopApps(Context context) {
    transactionViewNavigator.navigateToBrowser(context,
        Uri.parse("https://en.aptoide.com/store/bds-store/group/group-10867"));
  }

  public MutableLiveData<Boolean> shouldShowGamificationNotification() {
    return showNotification;
  }

  public void showTopUp(Context context) {
    transactionViewNavigator.openTopUp(context);
  }

  public MutableLiveData<Double> gamificationMaxBonus() {
    return gamificationMaxBonus;
  }

  public MutableLiveData<Double> onFetchTransactionsError() {
    return fetchTransactionsError;
  }

  public void showPromotions(Context context) {
    transactionViewNavigator.openPromotions(context);
  }
}
