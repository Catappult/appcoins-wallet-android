package com.asfoundation.wallet.ui.iab;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.appcoins.wallet.appcoins.rewards.Transaction;
import com.appcoins.wallet.billing.repository.entity.TransactionData;
import com.asf.wallet.R;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.util.CurrencyFormatUtils;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static com.asfoundation.wallet.analytics.FacebookEventLogger.EVENT_REVENUE_CURRENCY;
import static com.asfoundation.wallet.ui.iab.RewardsManager.RewardPayment.Status.FORBIDDEN;

public class AppcoinsRewardsBuyPresenter {
  private final AppcoinsRewardsBuyView view;
  private final RewardsManager rewardsManager;
  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;
  private final BigDecimal amount;
  private final String uri;
  private final String packageName;
  private final TransferParser transferParser;
  private final boolean isBds;
  private final BillingAnalytics analytics;
  private final TransactionBuilder transactionBuilder;
  private final CurrencyFormatUtils formatter;
  private final int gamificationLevel;
  private final AppcoinsRewardsBuyInteract appcoinsRewardsBuyInteract;

  AppcoinsRewardsBuyPresenter(AppcoinsRewardsBuyView view, RewardsManager rewardsManager,
      Scheduler viewScheduler, CompositeDisposable disposables, BigDecimal amount, String uri,
      String packageName, TransferParser transferParser, boolean isBds, BillingAnalytics analytics,
      TransactionBuilder transactionBuilder, CurrencyFormatUtils formatter, int gamificationLevel,
      AppcoinsRewardsBuyInteract appcoinsRewardsBuyInteract) {
    this.view = view;
    this.rewardsManager = rewardsManager;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.amount = amount;
    this.uri = uri;
    this.packageName = packageName;
    this.transferParser = transferParser;
    this.isBds = isBds;
    this.analytics = analytics;
    this.transactionBuilder = transactionBuilder;
    this.formatter = formatter;
    this.gamificationLevel = gamificationLevel;
    this.appcoinsRewardsBuyInteract = appcoinsRewardsBuyInteract;
  }

  public void present() {
    view.lockRotation();
    handleBuyClick();
    handleOkErrorClick();
    handleSupportClicks();
  }

  private void handleOkErrorClick() {
    disposables.add(view.getOkErrorClick()
        .subscribe(__ -> view.errorClose()));
  }

  private void handleBuyClick() {
    disposables.add(transferParser.parse(uri)
        .flatMapCompletable(transaction -> rewardsManager.pay(transaction.getSkuId(), amount,
            transaction.toAddress(), packageName, getOrigin(isBds, transaction),
            transaction.getType(), transaction.getPayload(), transaction.getCallbackUrl(),
            transaction.getOrderReference(), transaction.getReferrerUrl())
            .andThen(rewardsManager.getPaymentStatus(packageName, transaction.getSkuId(),
                transaction.amount()))
            .observeOn(viewScheduler)
            .flatMapCompletable(
                paymentStatus -> handlePaymentStatus(paymentStatus, transaction.getSkuId(),
                    transaction.amount())))
        .doOnSubscribe(disposable -> view.showLoading())
        .subscribe());
  }

  @Nullable private String getOrigin(boolean isBds, TransactionBuilder transaction) {
    if (transaction.getOrigin() == null) {
      return isBds ? "BDS" : null;
    } else {
      return transaction.getOrigin();
    }
  }

  private Completable handlePaymentStatus(RewardsManager.RewardPayment transaction, String sku,
      BigDecimal amount) {
    sendPaymentErrorEvent(transaction);
    switch (transaction.getStatus()) {
      case PROCESSING:
        return Completable.fromAction(view::showLoading);
      case COMPLETED:
        if (isBds && transactionBuilder.getType()
            .equalsIgnoreCase(TransactionData.TransactionType.INAPP.name())) {
          return rewardsManager.getPaymentCompleted(packageName, sku)
              .flatMapCompletable(purchase -> Completable.fromAction(view::showTransactionCompleted)
                  .subscribeOn(viewScheduler)
                  .andThen(Completable.timer(view.getAnimationDuration(), TimeUnit.MILLISECONDS))
                  .andThen(
                      Completable.fromAction(appcoinsRewardsBuyInteract::removeAsyncLocalPayment))
                  .andThen(Completable.fromAction(
                      () -> view.finish(purchase, transaction.getOrderReference()))))
              .observeOn(viewScheduler)
              .onErrorResumeNext(throwable -> Completable.fromAction(() -> {
                throwable.printStackTrace();
                view.showGenericError();
                view.hideLoading();
              }));
        }
        return rewardsManager.getTransaction(packageName, sku, amount)
            .firstOrError()
            .map(Transaction::getTxId)
            .doOnSuccess(view::finish)
            .ignoreElement();
      case ERROR:
        return Completable.fromAction(view::showGenericError);
      case FORBIDDEN:
        return Completable.fromAction(() -> view.showError(mapError(FORBIDDEN)));
      case NO_NETWORK:
        return Completable.fromAction(() -> {
          view.showNoNetworkError();
          view.hideLoading();
        });
    }
    return Completable.error(new UnsupportedOperationException(
        "Transaction status " + transaction.getStatus() + " not supported"));
  }

  public void stop() {
    disposables.clear();
  }

  void sendPaymentEvent() {
    analytics.sendPaymentEvent(packageName, transactionBuilder.getSkuId(),
        transactionBuilder.amount()
            .toString(), BillingAnalytics.PAYMENT_METHOD_REWARDS, transactionBuilder.getType());
  }

  void sendRevenueEvent() {
    analytics.sendRevenueEvent(formatter.scaleFiat(appcoinsRewardsBuyInteract.convertToFiat(
        transactionBuilder.amount()
            .doubleValue(), EVENT_REVENUE_CURRENCY)
        .blockingGet()
        .getAmount())
        .toString());
  }

  void sendPaymentSuccessEvent() {
    analytics.sendPaymentSuccessEvent(packageName, transactionBuilder.getSkuId(),
        transactionBuilder.amount()
            .toString(), BillingAnalytics.PAYMENT_METHOD_REWARDS, transactionBuilder.getType());
  }

  private void sendPaymentErrorEvent(RewardsManager.RewardPayment transaction) {
    if (transaction.getStatus() == RewardsManager.RewardPayment.Status.ERROR
        || transaction.getStatus() == RewardsManager.RewardPayment.Status.NO_NETWORK
        || transaction.getStatus() == RewardsManager.RewardPayment.Status.FORBIDDEN) {
      analytics.sendPaymentErrorEvent(packageName, transactionBuilder.getSkuId(),
          transactionBuilder.amount()
              .toString(), BillingAnalytics.PAYMENT_METHOD_REWARDS, transactionBuilder.getType(),
          transaction.getStatus()
              .toString());
    }
  }

  private void handleSupportClicks() {
    disposables.add(Observable.merge(view.getSupportIconClick(), view.getSupportLogoClick())
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .flatMapCompletable(__ -> appcoinsRewardsBuyInteract.showSupport(gamificationLevel))
        .subscribe());
  }

  @StringRes private int mapError(RewardsManager.RewardPayment.Status status) {
    if (status == FORBIDDEN) {
      return R.string.purchase_wallet_error_contact_us;
    } else {
      return R.string.activity_iab_error_message;
    }
  }
}