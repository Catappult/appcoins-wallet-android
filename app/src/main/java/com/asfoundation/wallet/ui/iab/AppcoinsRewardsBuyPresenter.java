package com.asfoundation.wallet.ui.iab;

import androidx.annotation.Nullable;
import com.appcoins.wallet.appcoins.rewards.Transaction;
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.util.CurrencyFormatUtils;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static com.asfoundation.wallet.analytics.FacebookEventLogger.EVENT_REVENUE_CURRENCY;

public class AppcoinsRewardsBuyPresenter {
  private final AppcoinsRewardsBuyView view;
  private final RewardsManager rewardsManager;
  private final Scheduler scheduler;
  private final CompositeDisposable disposables;
  private final BigDecimal amount;
  private final String uri;
  private final String packageName;
  private final TransferParser transferParser;
  private final boolean isBds;
  private final BillingAnalytics analytics;
  private final TransactionBuilder transactionBuilder;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final CurrencyFormatUtils formatter;

  AppcoinsRewardsBuyPresenter(AppcoinsRewardsBuyView view, RewardsManager rewardsManager,
      Scheduler scheduler, CompositeDisposable disposables, BigDecimal amount, String uri,
      String packageName, TransferParser transferParser, boolean isBds, BillingAnalytics analytics,
      TransactionBuilder transactionBuilder, InAppPurchaseInteractor inAppPurchaseInteractor,
      CurrencyFormatUtils formatter) {
    this.view = view;
    this.rewardsManager = rewardsManager;
    this.scheduler = scheduler;
    this.disposables = disposables;
    this.amount = amount;
    this.uri = uri;
    this.packageName = packageName;
    this.transferParser = transferParser;
    this.isBds = isBds;
    this.analytics = analytics;
    this.transactionBuilder = transactionBuilder;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.formatter = formatter;
  }

  public void present() {
    view.lockRotation();
    handleBuyClick();
    handleOkErrorClick();
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
            .observeOn(scheduler)
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
        if (isBds && isManagedPaymentType(transactionBuilder.getType())) {
          BillingSupportedType billingType =
              BillingSupportedType.valueOfManagedType(transactionBuilder.getType());
          return rewardsManager.getPaymentCompleted(packageName, sku, billingType)
              .flatMapCompletable(purchase -> Completable.fromAction(view::showTransactionCompleted)
                  .subscribeOn(scheduler)
                  .andThen(Completable.timer(view.getAnimationDuration(), TimeUnit.MILLISECONDS))
                  .andThen(Completable.fromAction(inAppPurchaseInteractor::removeAsyncLocalPayment))
                  .andThen(Completable.fromAction(
                      () -> view.finish(purchase, transaction.getOrderReference()))))
              .observeOn(scheduler)
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
        return Completable.fromAction(() -> {
          view.showGenericError();
          view.hideLoading();
        });
      case NO_NETWORK:
        return Completable.fromAction(() -> {
          view.showNoNetworkError();
          view.hideLoading();
        });
    }
    return Completable.error(new UnsupportedOperationException(
        "Transaction status " + transaction.getStatus() + " not supported"));
  }

  private boolean isManagedPaymentType(String type) {
    return type.equals("INAPP") || type.equals("SUBS");
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
    analytics.sendRevenueEvent(formatter.scaleFiat(inAppPurchaseInteractor.convertToFiat(
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
        || transaction.getStatus() == RewardsManager.RewardPayment.Status.NO_NETWORK) {
      analytics.sendPaymentErrorEvent(packageName, transactionBuilder.getSkuId(),
          transactionBuilder.amount()
              .toString(), BillingAnalytics.PAYMENT_METHOD_REWARDS, transactionBuilder.getType(),
          transaction.getStatus()
              .toString());
    }
  }
}