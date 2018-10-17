package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
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

  public AppcoinsRewardsBuyPresenter(AppcoinsRewardsBuyView view, RewardsManager rewardsManager,
      Scheduler scheduler, CompositeDisposable disposables, BigDecimal amount, String storeAddress,
      String oemAddress, String uri, String packageName, TransferParser transferParser,
      String productName) {
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
  }

  public void present() {
    handleBuyClick();
    handleViewSetup();
  }

  private void handleViewSetup() {
    view.showLoading();
    view.setupView(amount.setScale(2, RoundingMode.CEILING)
        .toPlainString(), productName, packageName);
    view.hideLoading();
    view.showPaymentDetails();
  }

  private void handleBuyClick() {
    disposables.add(view.getBuyClick()
        .flatMapSingle(__ -> transferParser.parse(uri))
        .flatMapCompletable(transaction -> rewardsManager.pay(transaction.getSkuId(), amount,
            transaction.toAddress(), storeAddress, oemAddress, packageName)
            .andThen(rewardsManager.getPaymentStatus(packageName, transaction.getSkuId()))
            .observeOn(scheduler)
            .flatMapCompletable(
                paymentStatus -> handlePaymentStatus(paymentStatus, transaction.getSkuId())))
        .subscribe());
  }

  private Completable handlePaymentStatus(RewardsManager.RewardPayment transaction, String sku) {
    switch (transaction.getStatus()) {
      case PROCESSING:
        return Completable.fromAction(() -> {
          view.hidePaymentDetails();
          view.showProcessingLoading();
        });
      case COMPLETED:
        return rewardsManager.getPaymentCompleted(packageName, sku)
            .doOnSuccess(view::finish)
            .ignoreElement();
      case ERROR:
        return Completable.fromAction(() -> {
          view.showPaymentDetails();
          view.hideGenericLoading();
        });
    }
    return Completable.error(new UnsupportedOperationException(
        "Transaction status " + transaction.getStatus() + " not supported"));
  }

  public void stop() {
    disposables.clear();
  }
}