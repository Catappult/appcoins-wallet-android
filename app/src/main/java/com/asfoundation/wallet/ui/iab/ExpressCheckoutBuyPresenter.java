package com.asfoundation.wallet.ui.iab;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class ExpressCheckoutBuyPresenter {
  private static final String TAG = ExpressCheckoutBuyPresenter.class.getSimpleName();
  private final ExpressCheckoutBuyView view;
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final Scheduler viewScheduler;
  private final CompositeDisposable disposables;

  public ExpressCheckoutBuyPresenter(ExpressCheckoutBuyView view,
      InAppPurchaseInteractor inAppPurchaseInteractor, Scheduler viewScheduler,
      CompositeDisposable disposables) {
    this.view = view;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
  }

  public void present(String uriString, String appPackage, String productName) {
    setupUi(uriString);
    handleCancelClick();
  }

  public void setupUi(String uriString) {
    disposables.add(inAppPurchaseInteractor.parseTransaction(uriString)
        .flatMapObservable(transactionBuilder -> inAppPurchaseInteractor.convertToFiat(
            transactionBuilder.amount()
                .doubleValue())
            .observeOn(viewScheduler)
            .doOnNext(value -> view.setup(transactionBuilder, value)))
        .subscribe(__ -> {
        }, this::showError));
  }

  private void handleCancelClick() {
    disposables.add(view.getCancelClick()
        .subscribe(click -> close()));
  }

  private void showError(Throwable t) {
  }

  private void close() {
    view.close();
  }
}
