package com.asfoundation.wallet.ui.iab;

import android.util.Log;
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
  private final String developerAddress = "0xd9BA3c6932a5084D0CA0769893353D60b23AAfC4";
  private final String storeAddress = "0xd9BA3c6932a5084D0CA0769893353D60b23AAfC4";
  private final String oemAddress = "0xd9BA3c6932a5084D0CA0769893353D60b23AAfC4";
  private final String sku = "cm.aptoide.pt:gas";
  private final String packageName;

  public AppcoinsRewardsBuyPresenter(AppcoinsRewardsBuyView view, RewardsManager rewardsManager,
      Scheduler scheduler, CompositeDisposable disposables, BigDecimal amount, String packageName) {
    this.view = view;
    this.rewardsManager = rewardsManager;
    this.scheduler = scheduler;
    this.disposables = disposables;
    this.amount = amount;
    this.packageName = packageName;
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
        .observeOn(scheduler)
        .doOnNext(__ -> view.hidePaymentDetails())
        .doOnNext(__ -> view.showLoading())
        .flatMapSingle(
            __ -> rewardsManager.pay(sku, amount, developerAddress, storeAddress, oemAddress)
                .doOnComplete(() -> Log.d(TAG, "handleBuyClick() called"))
                .andThen(rewardsManager.getPaymentCompleted(packageName, sku)))
        .doOnNext(purchase -> view.finish())
        .subscribe());
  }

  public void stop() {
    disposables.clear();
  }
}