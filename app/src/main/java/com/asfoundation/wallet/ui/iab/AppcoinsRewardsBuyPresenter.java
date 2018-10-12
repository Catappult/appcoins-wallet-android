package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.appcoins.rewards.Transaction;
import com.asfoundation.wallet.util.TransferParser;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import java.math.BigDecimal;

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

  public AppcoinsRewardsBuyPresenter(AppcoinsRewardsBuyView view, RewardsManager rewardsManager,
      Scheduler scheduler, CompositeDisposable disposables, BigDecimal amount, String storeAddress,
      String oemAddress, String uri, String packageName, TransferParser transferParser) {
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
  }

  public void present() {
    handleBuyClick();
    handleViewSetup();
  }

  private void handleViewSetup() {
    view.setAmount(amount.toPlainString());
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

  private Completable handlePaymentStatus(Transaction transaction, String sku) {
    switch (transaction.getStatus()) {
      case PROCESSING:
        return Completable.fromAction(() -> {
          view.hidePaymentDetails();
          view.showLoading();
        });
      case COMPLETED:
        return rewardsManager.getPaymentCompleted(packageName, sku)
            .doOnSuccess(view::finish)
            .ignoreElement();
      case ERROR:
        return Completable.fromAction(() -> {
          view.showPaymentDetails();
          view.hideLoading();
        });
    }
    return Completable.error(new UnsupportedOperationException(
        "Transaction status " + transaction.getStatus() + " not supported"));
  }

  public void stop() {
    disposables.clear();
  }
}