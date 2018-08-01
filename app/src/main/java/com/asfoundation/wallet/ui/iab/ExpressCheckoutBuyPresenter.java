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

  public void present(String uriString) {
    setupUi(uriString);
    handleCancelClick();
  }

  private void setupUi(String uriString) {
    disposables.add(inAppPurchaseInteractor.parseTransaction(uriString)
        .flatMap(transactionBuilder -> inAppPurchaseInteractor.convertToFiat(
            transactionBuilder.amount()
                .doubleValue())
            .observeOn(viewScheduler)
            .doOnSuccess(value -> view.setup(transactionBuilder, value)))
        .subscribe(__ -> {
        }, this::showError));
  }

  private void handleCancelClick() {
    disposables.add(view.getCancelClick()
        .subscribe(click -> close()));
  }

  private void showError(Throwable t) {
  }

  public void stop() {
    if (!disposables.isDisposed()) {
      disposables.clear();
    }
  }

  private void close() {
    view.close();
  }
}
