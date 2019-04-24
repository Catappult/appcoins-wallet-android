package com.asfoundation.wallet.ui.iab;

import androidx.annotation.Nullable;
import com.appcoins.wallet.appcoins.rewards.Transaction;
import com.appcoins.wallet.billing.repository.entity.TransactionData;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.asfoundation.wallet.analytics.FacebookEventLogger.EVENT_REVENUE_CURRENCY;

public class AppcoinsRewardsBuyPresenter {
  private final AppcoinsRewardsBuyView view;
  private final RewardsManager rewardsManager;
  private final Scheduler scheduler;
  private final CompositeDisposable disposables;
  private final BigDecimal amount;
  private final String oemAddress;
  private final String uri;
  private final String packageName;
  private final TransferParser transferParser;
  private final String productName;
  private final boolean isBds;
  private final BillingAnalytics analytics;
  private final TransactionBuilder transactionBuilder;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;

  public AppcoinsRewardsBuyPresenter(AppcoinsRewardsBuyView view, RewardsManager rewardsManager,
      Scheduler scheduler, CompositeDisposable disposables, BigDecimal amount, String oemAddress,
      String uri, String packageName, TransferParser transferParser, String productName,
      boolean isBds, BillingAnalytics analytics, TransactionBuilder transactionBuilder,
      InAppPurchaseInteractor inAppPurchaseInteractor) {
    this.view = view;
    this.rewardsManager = rewardsManager;
    this.scheduler = scheduler;
    this.disposables = disposables;
    this.amount = amount;
    this.oemAddress = oemAddress;
    this.uri = uri;
    this.packageName = packageName;
    this.transferParser = transferParser;
    this.productName = productName;
    this.isBds = isBds;
    this.analytics = analytics;
    this.transactionBuilder = transactionBuilder;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
  }

  public void present() {
    handleBuyClick();
    handleCancelClick();
    handleViewSetup();
    handleOkErrorClick();
  }

  private void handleOkErrorClick() {
    disposables.add(view.getOkErrorClick()
        .subscribe(__ -> view.errorClose()));
  }

  private void handleCancelClick() {
    disposables.add(view.getCancelClick()
        .subscribe(__ -> view.close()));
  }

  private void handleViewSetup() {
    disposables.add(transferParser.parse(uri)
        .observeOn(scheduler)
        .doOnSuccess(transactionBuilder -> {
          view.showLoading();
          view.setupView(amount.setScale(2, RoundingMode.CEILING)
                  .toPlainString(), productName, packageName,
              TransactionData.TransactionType.DONATION.name()
                  .equalsIgnoreCase(transactionBuilder.getType()));
          view.hideLoading();
          view.showPaymentDetails();
        })
        .subscribe());
  }

  private void handleBuyClick() {
    disposables.add(view.getBuyClick()
        .flatMapSingle(__ -> transferParser.parse(uri))
        .flatMapCompletable(transaction -> rewardsManager.pay(transaction.getSkuId(), amount,
            transaction.toAddress(), packageName, getOrigin(isBds, transaction),
            transaction.getType(), transaction.getPayload(), transaction.getCallbackUrl(),
            transaction.getOrderReference())
            .andThen(rewardsManager.getPaymentStatus(packageName, transaction.getSkuId(),
                transaction.amount()))
            .observeOn(scheduler)
            .flatMapCompletable(
                paymentStatus -> handlePaymentStatus(paymentStatus, transaction.getSkuId(),
                    transaction.amount())))
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
    switch (transaction.getStatus()) {
      case PROCESSING:
        return Completable.fromAction(() -> {
          view.hidePaymentDetails();
          view.showProcessingLoading();
        });
      case COMPLETED:
        if (isBds && transactionBuilder.getType()
            .equalsIgnoreCase(TransactionData.TransactionType.INAPP.name())) {
          return rewardsManager.getPaymentCompleted(packageName, sku)
              .doOnSuccess(purchase -> view.finish(purchase, transaction.getOrderReference()))
              .ignoreElement()
              .observeOn(scheduler)
              .onErrorResumeNext(throwable -> Completable.fromAction(() -> {
                throwable.printStackTrace();
                view.showGenericError();
                view.hideGenericLoading();
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
          view.hideGenericLoading();
        });
      case NO_NETWORK:
        return Completable.fromAction(() -> {
          view.showNoNetworkError();
          view.hideGenericLoading();
        });
    }
    return Completable.error(new UnsupportedOperationException(
        "Transaction status " + transaction.getStatus() + " not supported"));
  }

  public void stop() {
    disposables.clear();
  }

  public void sendPaymentEvent(String purchaseDetails) {
    analytics.sendPaymentEvent(packageName, transactionBuilder.getSkuId(),
        transactionBuilder.amount()
            .toString(), purchaseDetails, transactionBuilder.getType());
  }

  public void sendRevenueEvent() {
    analytics.sendRevenueEvent(inAppPurchaseInteractor.convertToFiat(transactionBuilder.amount()
        .doubleValue(), EVENT_REVENUE_CURRENCY)
        .blockingGet()
        .getAmount()
        .setScale(2, BigDecimal.ROUND_UP)
        .toString());
  }
}