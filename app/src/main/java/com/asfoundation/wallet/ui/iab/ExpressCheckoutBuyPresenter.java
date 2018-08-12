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

  public void present(double transactionValue, String currency) {
    setupUi(transactionValue, currency);
    handleCancelClick();
    handleErrorDismisses();
  }

  private void setupUi(double transactionValue, String currency) {
    disposables.add(inAppPurchaseInteractor.convertToFiat(transactionValue, currency)
            .observeOn(viewScheduler)
        .doOnSuccess(view::setup)
        .subscribe(__ -> {
        }, this::showError));
  }

  private void handleCancelClick() {
    disposables.add(view.getCancelClick()
        .subscribe(click -> close()));
  }

  private void showError(Throwable t) {
    view.showError();
  }

  public void stop() {
    if (!disposables.isDisposed()) {
      disposables.clear();
    }
  }

  private void close() {
    view.close();
  }

  private void handleErrorDismisses() {
    disposables.add(view.errorDismisses()
        .doOnNext(__ -> close())
        .subscribe(__ -> {
        }, this::showError));
  }


}
