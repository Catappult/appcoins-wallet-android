package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.billing.BillingMessagesMapper;
import com.appcoins.wallet.billing.mappers.ExternalBillingSerializer;
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
  private final BillingMessagesMapper billingMessagesMapper;
  private final ExternalBillingSerializer billingSerializer;

  public ExpressCheckoutBuyPresenter(ExpressCheckoutBuyView view,
      InAppPurchaseInteractor inAppPurchaseInteractor, Scheduler viewScheduler,
      CompositeDisposable disposables,
      BillingMessagesMapper billingMessagesMapper, ExternalBillingSerializer billingSerializer) {
    this.view = view;
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.viewScheduler = viewScheduler;
    this.disposables = disposables;
    this.billingMessagesMapper = billingMessagesMapper;
    this.billingSerializer = billingSerializer;
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
    view.close(billingMessagesMapper.mapCancellation());
  }


  private void handleErrorDismisses() {
    disposables.add(view.errorDismisses()
        .doOnNext(__ -> close())
        .subscribe(__ -> {
        }, this::showError));
  }


}
