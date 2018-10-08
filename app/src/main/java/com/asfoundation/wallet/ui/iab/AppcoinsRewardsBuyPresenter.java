package com.asfoundation.wallet.ui.iab;

import io.reactivex.disposables.CompositeDisposable;
import java.math.BigDecimal;
import rx.Scheduler;

public class AppcoinsRewardsBuyPresenter {
  private final AppcoinsRewardsBuyView view;
  private final Scheduler scheduler;
  private final RewardsManager rewardsManager;
  private final CompositeDisposable disposables;
  private final BigDecimal amount;

  public AppcoinsRewardsBuyPresenter(AppcoinsRewardsBuyView view, Scheduler scheduler,
      RewardsManager rewardsManager, CompositeDisposable disposables, BigDecimal amount) {
    this.view = view;
    this.scheduler = scheduler;
    this.rewardsManager = rewardsManager;
    this.disposables = disposables;
    this.amount = amount;
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
        .doOnNext(__ -> view.hidePaymentDetails())
        .doOnNext(__ -> view.showLoading())
        .flatMapCompletable(__ -> rewardsManager.pay("cm.aptoide.pt:gas", amount,
            "0xd9BA3c6932a5084D0CA0769893353D60b23AAfC4",
            "0xd9BA3c6932a5084D0CA0769893353D60b23AAfC4",
            "0xd9BA3c6932a5084D0CA0769893353D60b23AAfC4"))
        .subscribe());
  }

  public void stop() {
    disposables.clear();
  }
}