package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.appcoins.rewards.Transaction;
import com.appcoins.wallet.appcoins.rewards.TransactionIdRepository;
import com.appcoins.wallet.billing.repository.entity.TransactionData;
import com.asfoundation.wallet.billing.analytics.BillingAnalytics;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class AppcoinsRewardsBuyPresenter {
  private static final String TAG = AppcoinsRewardsBuyPresenter.class.getSimpleName();
  private final AppcoinsRewardsBuyView view;
  private final RewardsManager rewardsManager;
  private final Scheduler scheduler;
  private final CompositeDisposable disposables;
  private final BigDecimal amount;
  private final String storeAddress;
  private final String oemAddress;
  private final String uri;
  private final String packageName;
  private final TransferParser transferParser;
  private final String productName;
  private final boolean isBds;
  private final BillingAnalytics analytics;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final Single<TransactionBuilder> transactionBuilder;

  private final TransactionIdRepository transactionIdRepository;

  public AppcoinsRewardsBuyPresenter(TransactionIdRepository transactionIdRepository,
      AppcoinsRewardsBuyView view, RewardsManager rewardsManager, Scheduler scheduler,
      CompositeDisposable disposables, BigDecimal amount, String storeAddress, String oemAddress,
      String uri, String packageName, TransferParser transferParser, String productName,
      boolean isBds, BillingAnalytics analytics, InAppPurchaseInteractor inAppPurchaseInteractor) {
    this.transactionIdRepository = transactionIdRepository;
    this.view = view;
    this.rewardsManager = rewardsManager;
    this.scheduler = scheduler;
    this.disposables = disposables;
    this.amount = amount;
    this.storeAddress = storeAddress;
    this.oemAddress = oemAddress;
    this.uri = uri;
    this.packageName = packageName;
    this.transferParser = transferParser;
    this.productName = productName;
    this.isBds = isBds;
    this.analytics = analytics;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.transactionBuilder = inAppPurchaseInteractor.parseTransaction(uri, isBds);
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
            transaction.toAddress(), storeAddress, oemAddress, packageName,
            isBds ? Transaction.Origin.BDS : Transaction.Origin.UNKNOWN, transaction.getType(),
            transaction.getPayload(), transaction.getCallbackUrl())
            .andThen(rewardsManager.getPaymentStatus(packageName, transaction.getSkuId(), transaction.amount()))
            .observeOn(scheduler)
            .flatMapCompletable(
                paymentStatus -> handlePaymentStatus(paymentStatus, transaction.getSkuId(), transaction.amount())))
        .subscribe());
  }

  private Completable handlePaymentStatus(RewardsManager.RewardPayment transaction, String sku, BigDecimal amount) {
    switch (transaction.getStatus()) {
      case PROCESSING:
        return Completable.fromAction(() -> {
          view.hidePaymentDetails();
          view.showProcessingLoading();
        });
      case COMPLETED:
        if (isBds) {
          return rewardsManager.getPaymentCompleted(packageName, sku)
              .doOnSuccess(view::finish)
              .ignoreElement()
              .observeOn(scheduler)
              .onErrorResumeNext(throwable -> Completable.fromAction(() -> {
                throwable.printStackTrace();
                view.showGenericError();
                view.hideGenericLoading();
              }));
        }
        return Completable.fromAction(() -> view.finish(
            rewardsManager.getTransaction(packageName, sku, amount)
                .map(Transaction::getTxId)
                .blockingFirst()));
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

  public void sendPurchaseDetails(String purchaseDetails) {
    disposables.add(transactionBuilder.subscribe(
        transactionBuilder -> analytics.sendPurchaseDetailsEvent(packageName,
            transactionBuilder.getSkuId(), transactionBuilder.amount()
                .toString(), purchaseDetails, transactionBuilder.getType())));
  }

  public void sendPaymentEvent(String purchaseDetails) {
    disposables.add(transactionBuilder.subscribe(
        transactionBuilder -> analytics.sendPaymentEvent(packageName, transactionBuilder.getSkuId(),
            transactionBuilder.amount()
                .toString(), purchaseDetails, transactionBuilder.getType())));
  }

  public void sendRevenueEvent() {
    disposables.add(transactionBuilder.subscribe(transactionBuilder -> analytics.sendRevenueEvent(
        transactionBuilder.amount()
            .toString())));
  }
}